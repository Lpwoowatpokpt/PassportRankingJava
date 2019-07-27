package com.lpwoowatpokpt.passportrankingjava.UI.Fragments;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.lpwoowatpokpt.passportrankingjava.Adapter.CompareAdapter;
import com.lpwoowatpokpt.passportrankingjava.Common.Common;
import com.lpwoowatpokpt.passportrankingjava.Common.TinyDB;
import com.lpwoowatpokpt.passportrankingjava.ExcelModel.Cell;
import com.lpwoowatpokpt.passportrankingjava.ExcelModel.ColTitle;
import com.lpwoowatpokpt.passportrankingjava.ExcelModel.RowTitle;
import com.lpwoowatpokpt.passportrankingjava.Model.Ranking;
import com.lpwoowatpokpt.passportrankingjava.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import cn.zhouchaoyuan.excelpanel.ExcelPanel;
import es.dmoral.toasty.Toasty;
import in.galaxyofandroid.spinerdialog.SpinnerDialog;

/**
 * A simple {@link Fragment} subclass.
 */
public class CompareFragment extends Fragment{

    private Context context;
    private TinyDB tinyDB;
    private FloatingActionButton fab;

    private final static int PAGE_SIZE = 1;
    private final static int ROW_SIZE = Common.countryModel.size();

    private ProgressBar progress;
    private CompareAdapter compareAdapter;

    private SpinnerDialog spinnerDialog;

    private List<RowTitle> rowTitles;
    private List<List<Cell>> cells;

    public static CompareFragment newInstance(Context context) {
        return new CompareFragment(context);
    }

    private CompareFragment(Context context) {
        this.context = context;
        tinyDB = new TinyDB(context);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View myFragment = inflater.inflate(R.layout.fragment_compare, container, false);
        progress = myFragment.findViewById(R.id.progress);
        ExcelPanel excelPanel = myFragment.findViewById(R.id.compare_container);

        compareAdapter = new CompareAdapter(context, onClickListener);
        excelPanel.setAdapter(compareAdapter);
        excelPanel.addOnScrollListener(onScrollListener);

        initData();

        fab = myFragment.findViewById(R.id.fabAdd);
        fab.setOnClickListener(view -> {
            showSpinner();
            spinnerDialog.showSpinerDialog();
        });

        return myFragment;
    }


    private void initData() {
        progress.setVisibility(View.GONE);
        rowTitles = generateRowData();
        cells = genCellData();
        compareAdapter.setAllData(genColTitles(), rowTitles,  cells);
    }


    private ExcelPanel.OnScrollListener onScrollListener = new ExcelPanel.OnScrollListener() {
        @Override
        public void onScrolled(ExcelPanel excelPanel, int dx, int dy) {
            super.onScrolled(excelPanel, dx, dy);
            if (dy > 0)
                fab.hide();
            else if (dy < 0)
                fab.show();


            if (dx > 0)
                fab.hide();
            else if (dx < 0)
                fab.show();


        }
    };

    private View.OnClickListener onClickListener = view -> {
    };

    private void showSpinner() {
        spinnerDialog = new SpinnerDialog(getActivity(), tinyDB.getListString(Common.COUNTRY_LIST), getString(R.string.select_to_compare), R.style.DialogAnimations_SmileWindow, "Close");

        spinnerDialog.setCancellable(true);
        spinnerDialog.setShowKeyboard(false);

        spinnerDialog.setTitleColor(getResources().getColor(R.color.colorAccent));
        spinnerDialog.setTitleColor(getResources().getColor(R.color.colorPrimaryText));
        spinnerDialog.setCloseColor(getResources().getColor(R.color.visa_requiered));

        spinnerDialog.bindOnSpinerListener((s, pos) -> {

            String countryNameToCompare = Common.countryModel.get(pos).getName();
            String coverToCompare = Common.countryModel.get(pos).getCover();

            for (int i = 0; i < rowTitles.size(); i++) {
                if (countryNameToCompare.matches(rowTitles.get(i).getCountryName())) {
                    Toasty.warning(context, countryNameToCompare + " " + getString(R.string.country_exists), Toasty.LENGTH_LONG).show();
                    spinnerDialog.closeSpinerDialog();
                    return;
                }
            }

            addNewColumn(countryNameToCompare,coverToCompare);
        });
    }

    private void addNewColumn(String countryNameToCompare, String coverToCompare) {
        DatabaseReference topRanking = Common.getDatabase().getReference(Common.Top);
        topRanking.orderByKey().equalTo(countryNameToCompare).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnap : dataSnapshot.getChildren()) {
                    Ranking ranking = postSnap.getValue(Ranking.class);
                    assert ranking != null;
                    Integer totalScore = ranking.getTotalScore();
                    RowTitle rowTitle = new RowTitle();
                    rowTitle.setCountryName(countryNameToCompare);
                    rowTitle.setCover(coverToCompare);
                    rowTitle.setMobilityScore(totalScore);
                    rowTitles.add(rowTitle);

                    DatabaseReference countries = Common.getDatabase().getReference(Common.Countries);
                    Query query = countries.orderByKey().equalTo(countryNameToCompare);

                    query.addListenerForSingleValueEvent(new ValueEventListener() {
                        @RequiresApi(api = Build.VERSION_CODES.N)
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for (DataSnapshot postSnap : dataSnapshot.getChildren()) {
                                Map<String, Long> data = (Map) postSnap.getValue();
                                assert data != null;
                                Map<String, Long> treeMap = new TreeMap<>(data);

                                ArrayList<Long> status = new ArrayList<>();

                                for (Map.Entry<String, Long> entry : treeMap.entrySet()) {
                                    status.add(entry.getValue());
                                }

                                List<List<Cell>> cells1 = updateData(status);

                                for (int i = 0; i < cells.size(); i++) {
                                    cells1.get(i).addAll(0, cells.get(i));
                                }

                                compareAdapter.setAllData(genColTitles(), rowTitles, cells1);
                                cells = cells1;

                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Toasty.error(context, getString(R.string.error_toast) + databaseError.getMessage(), 5).show();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toasty.error(context, getString(R.string.error_toast) + databaseError.getMessage(), 5).show();
            }
        });
    }


    private List<ColTitle> genColTitles() {
        List<ColTitle> colTitles = new ArrayList<>();
        for (int i = 0; i < ROW_SIZE; i++) {
            ColTitle colTitle = new ColTitle();
            colTitle.setName(tinyDB.getListString(Common.COUNTRY_LIST).get(i));
            colTitle.setImage(tinyDB.getListString(Common.FLAG_LIST).get(i));
            colTitles.add(colTitle);
        }
        return colTitles;
    }

    private ArrayList<RowTitle> generateRowData() {
        ArrayList<RowTitle> rowTitles = new ArrayList<>();
        for (int i = 0; i < PAGE_SIZE; i++) {
            RowTitle rowTitle = new RowTitle();
            rowTitle.setCover(tinyDB.getString(Common.COVER));
            rowTitle.setCountryName(tinyDB.getString(Common.COUNTRY_NAME));
            rowTitle.setMobilityScore(tinyDB.getInt(Common.MOBILITY_SCORE));
            rowTitles.add(rowTitle);
        }
        return rowTitles;
    }

    private List<List<Cell>> genCellData() {
        List<List<Cell>> cells = new ArrayList<>();
        for (int i = 0; i < ROW_SIZE; i++) {
            List<Cell> cellList = new ArrayList<>();
            cells.add(cellList);
            for (int j = 0; j < PAGE_SIZE; j++) {
                Cell cell = new Cell();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    cell.setStatus(Math.toIntExact(tinyDB.getListLong(Common.STATUS).get(i)));
                    cellList.add(cell);
                }
            }
        }
        return cells;
    }

    private List<List<Cell>> updateData(ArrayList<Long> status) {
        List<List<Cell>> cells = new ArrayList<>();
        for (int i = 0; i < ROW_SIZE; i++) {
            List<Cell> cellList = new ArrayList<>();
            cells.add(cellList);
            for (int j = 0; j < PAGE_SIZE; j++) {
                Cell cell = new Cell();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    cell.setStatus(Math.toIntExact(status.get(i)));
                    cellList.add(cell);
                }
            }
        }
        return cells;
    }


}
