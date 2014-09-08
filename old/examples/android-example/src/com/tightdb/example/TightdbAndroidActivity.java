/*
 * Copyright 2014 Realm Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.realm.example;

import java.io.File;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.ViewGroup.LayoutParams;

// import com.stocks.generate;

import io.realm.Group;
import io.realm.DefineTable;
import org.stockchart.StockChartView;
import org.stockchart.core.Area;
import org.stockchart.core.Axis;
import org.stockchart.core.Axis.ILabelFormatProvider;
import org.stockchart.core.AxisRange;
import org.stockchart.points.StockPoint;
import org.stockchart.series.AbstractSeries;
import org.stockchart.series.StockSeries;

public class TightdbAndroidActivity extends Activity {

    @DefineTable(row = "Stocka")
    class stocka {
        String Date;
        int Open;
        int High;
        int Low;
        int Close;
        int Volume;
        int Adj_Close;
    }

    private Handler mHandler = new Handler();
    private StockSeries s1 = new StockSeries();

    private static long tttt = 0;
    StockChartView s;
    private Runnable mUpdateTimeTask = new Runnable() {
        public void run() {

            // new generate().stocks();

            File dir = Environment.getExternalStorageDirectory();
//          File file = new File(dir, "NOK.realm");
            File file = new File(dir, "stocks_ZBRA.realm");

            Group group = new Group(file);
            StockaTable stocks = new StockaTable(group);

            Log.i("STOCK", stocks.getName() + "SSS" );
            for (Stocka stock : stocks) {
                Log.i("STOCK", "BLA!" + stock.getDate() +" " + Long.toString(stock.getHigh()) );
                StockPoint ss = new StockPoint(++tttt);
                ss.setLow(stock.getLow());
                ss.setHigh(stock.getHigh());
                ss.setClose(stock.getClose());
                ss.setOpen(stock.getOpen());
                s1.addPoint(ss);
            }
            s.invalidate();
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        s = new StockChartView(this);

        AxisRange ar = new AxisRange();
        ar.setMovable(true);
        ar.setZoomable(true);

        s.enableGlobalAxisRange(Axis.Side.BOTTOM, ar);

        Area a1 = new Area();
        Area a2 = new Area();
        a1.getRightAxis().setLabelFormatProvider(new ILabelFormatProvider()
        {
            @Override
            public String getAxisLabel(Axis sender, double value) {
                return String.valueOf(value);
            }
        });

        a2.setAutoHeight(false);
        a2.setHeightInPercents(0.2f);
        a2.getBottomAxis().setLabelFormatProvider(new ILabelFormatProvider()
        {
            @Override
            public String getAxisLabel(Axis sender, double value)
            {
                Area a = sender.getParent();

                for(int i=0;i<a.getSeriesCount();i++)
                {
                    AbstractSeries s = a.getSeriesAt(i);

                    int index = s.convertToArrayIndex(value);
                    if(index >=0 && index < s.getPointCount())
                    {
                        Object id = s.getPointAt(index).getID();

                        if(null != id)
                            return id.toString();
                    }
                }
                return null;
            }
        });

        a1.getLeftAxis().setVisible(false);
        a1.getTopAxis().setVisible(false);

        a2.getLeftAxis().setVisible(false);
        a2.getTopAxis().setVisible(false);

        a1.addSeries(s1);

        s.addArea(a1);

        setContentView(s, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));

        mHandler.removeCallbacks(mUpdateTimeTask);
        mHandler.postDelayed(mUpdateTimeTask, 100);
    }
}
