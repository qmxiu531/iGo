package com.gionee.autotest.traversal.ui;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.text.SpannableString;
import android.text.TextPaint;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.gionee.autotest.traversal.R;
import com.gionee.autotest.traversal.common.report.ConfigInfo;
import com.gionee.autotest.traversal.common.report.ExceptionInfo;
import com.gionee.autotest.traversal.common.report.ReportDetail;
import com.gionee.autotest.traversal.common.report.ReportSummary;
import com.gionee.autotest.traversal.common.util.Constant;
import com.gionee.autotest.traversal.common.util.Util;
import com.gionee.autotest.traversal.task.ConvertFileToJsonTask;
import com.gionee.autotest.traversal.widget.MarqueeTextView;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;

import java.io.File;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;

/**
 * Created by viking on 9/11/17.
 *
 * show single result activity
 */

public class ResultActivity extends BaseActivity {
    String timestamp  ;
    File json ;
    File html ;
    ReportSummary reportSummary ;

    private ConvertFileToJsonTask convertTask ;

    //Title information
    @Bind(R.id.result_title)
    TextView resultTitleView ;

    //Summary information
    @Bind(R.id.summary_package)
    MarqueeTextView summaryPkg ;

    @Bind(R.id.summary_timestamp)
    MarqueeTextView summaryTimeStamp ;

    @Bind(R.id.summary_duration)
    MarqueeTextView summaryDuration ;

    @Bind(R.id.summary_cover)
    MarqueeTextView summaryCover ;

    @Bind(R.id.summary_activities)
    MarqueeTextView summaryActivities ;

    @Bind(R.id.summary_not_pass_reason)
    MarqueeTextView summaryFailReason ;

    //activities information
    @Bind(R.id.layout_activity)
    FrameLayout layoutActivity ;

    @Bind(R.id.mPieChart)
    PieChart mPieChart ;

    @Bind(R.id.activity_info)
    ImageView mAImageInfo ;

    //Configuration information
/*    @Bind(R.id.config_max_steps)
    MarqueeTextView configMaxSteps ;*/

    @Bind(R.id.config_max_runtime)
    MarqueeTextView configMaxRuntime ;

/*    @Bind(R.id.config_app_restart_times)
    MarqueeTextView configAppRestartTimes;

    @Bind(R.id.config_log_mode)
    MarqueeTextView configLogMode;*/

    //Exception information
    @Bind(R.id.exception_info)
    ImageView mExceptionInfo ;

    @Bind(R.id.exception_title_crash)
    MarqueeTextView mTextCrash ;

    @Bind(R.id.exception_title_anr)
    MarqueeTextView mTextAnr ;

    @Override
    protected int layoutResId() {
        return R.layout.layout_result;
    }

    @Override
    protected int menuResId() {
        return R.menu.menu_detail;
    }

    @Override
    protected boolean isDisplayHomeUpEnabled() {
        //disable it
        return false;
    }

    @SuppressLint("StaticFieldLeak")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initViews() ;
        //check intent
        Intent result = getIntent() ;
        if (result == null || result.getStringExtra(Constant.EXTRA_TIMESTAMP) == null){
            Toast.makeText(getApplicationContext(), R.string.no_timestamp, Toast.LENGTH_SHORT).show();
            return ;
        }
        timestamp = result.getStringExtra(Constant.EXTRA_TIMESTAMP) ;
        Log.i(Constant.TAG, "result timestamp : " + timestamp) ;
        //check timestamp exist or not
        if (!Util.isTimeStampExist(timestamp)){
            Toast.makeText(getApplicationContext(), R.string.timestamp_not_exist, Toast.LENGTH_SHORT).show();
            return ;
        }

        //render it
        json = new File(Environment.getExternalStorageDirectory(), Constant.ROOT_NAME + File.separator + timestamp + File.separator
                            + Constant.DIR_REPORT + File.separator + Constant.DIR_REPORT_JSON) ;
        html = new File(Environment.getExternalStorageDirectory(), Constant.ROOT_NAME + File.separator + timestamp + File.separator
                            + Constant.DIR_REPORT + File.separator + Constant.DIR_REPORT_HTML) ;
        Log.i(Constant.TAG, "json path : " + json.getAbsolutePath()) ;
        Log.i(Constant.TAG, "html path : " + html.getAbsolutePath()) ;
        convertTask = new ConvertFileToJsonTask(this, json){
            @Override
            protected void onPostExecute(ReportSummary summary) {
                super.onPostExecute(summary);
                if (summary != null){
                    reportSummary = summary ;
                    Log.i(Constant.TAG, "test time : " + summary.testTime) ;
                    Log.i(Constant.TAG, "test total activities : " + summary.totalActivities) ;

                    renderViews() ;
                }
            }
        } ;
        convertTask.execute() ;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.web_content:
                showWebContent();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showWebContent() {
        if (html == null || !html.exists()){
            Toast.makeText(ResultActivity.this, R.string.local_html_not_exist_msg, Toast.LENGTH_SHORT).show();
            return ;
        }
        Intent wIntent = new Intent(this, WebviewActivity.class) ;
        wIntent.putExtra("html", html.getAbsolutePath()) ;
        startActivity(wIntent);
    }

    private void initViews() {
        mPieChart.setUsePercentValues(true);
        mPieChart.setDrawEntryLabels(false);
        mPieChart.getDescription().setEnabled(false);
        mPieChart.setExtraOffsets(5, 10, 5, 5);
        mPieChart.setDragDecelerationFrictionCoef(0.95f);

        mPieChart.setCenterText(generateCenterSpannableText());
        mPieChart.setExtraOffsets(20.f, 0.f, 20.f, 0.f);

        mPieChart.setDrawHoleEnabled(true);
        mPieChart.setHoleColor(Color.WHITE);

        mPieChart.setTransparentCircleColor(Color.WHITE);
        mPieChart.setTransparentCircleAlpha(110);

        mPieChart.setHoleRadius(58f);
        mPieChart.setTransparentCircleRadius(61f);

        mPieChart.setDrawCenterText(true);

        mPieChart.setRotationAngle(0);
        mPieChart.setRotationEnabled(false);
        mPieChart.setHighlightPerTapEnabled(false);

    }

    private void renderPieChart(final ReportDetail.ActivityDetail activities, String totalActivities){
        if (!(activities.hasNActivities || activities.hasTActivities)){
            layoutActivity.setVisibility(View.GONE);
            return ;
        }
        List<ReportDetail.ActivityItem> NActivities = activities.nActivities ;
        List<ReportDetail.ActivityItem> TActivities = activities.tActivities ;
        ArrayList<PieEntry> entries = new ArrayList<>();
        int numActivities = Integer.parseInt(totalActivities) ;
        NumberFormat numberFormat = NumberFormat.getInstance();
        numberFormat.setMaximumFractionDigits(2);

        //first traversal activities
        boolean hasSuccess = false ;
        if (TActivities != null && TActivities.size() > 0){
            String result = numberFormat.format(((float)TActivities.size()/(float)numActivities) * 100);
            Log.i(Constant.TAG, "traversal activities percentage :" + result + "%");
            entries.add(new PieEntry(Float.parseFloat(result), "已测试界面"));
            hasSuccess = true ;
        }

        //second not traversal activities
        boolean hasFail = false ;
        if (NActivities != null && NActivities.size() > 0){
            String result = numberFormat.format(((float)NActivities.size()/(float)numActivities) * 100);
            Log.i(Constant.TAG, "not traversal activities percentage :" + result + "%");
            entries.add(new PieEntry(Float.parseFloat(result), "未测试界面"));
            hasFail = true ;
        }

        setData(entries, hasSuccess, hasFail);

        mPieChart.animateY(1400, Easing.EasingOption.EaseInOutQuad);

        Legend mLegend = mPieChart.getLegend();
        mLegend.setEnabled(true);
        mLegend.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        mLegend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        mLegend.setOrientation(Legend.LegendOrientation.VERTICAL);
        mLegend.setDrawInside(false);

//        mLegend.setPosition(Legend.LegendPosition.BELOW_CHART_LEFT);  //左下边显示
        mLegend.setFormSize(12f);//比例块字体大小
        mLegend.setXEntrySpace(2f);//设置距离饼图的距离，防止与饼图重合
        mLegend.setYEntrySpace(2f);
        //设置比例块换行...
        mLegend.setWordWrapEnabled(true);
        mLegend.setDirection(Legend.LegendDirection.LEFT_TO_RIGHT);

        mLegend.setTextColor(getResources().getColor(R.color.colorDark));
        mLegend.setForm(Legend.LegendForm.SQUARE);//设置比例块形状，默认为方块

        mAImageInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ResultActivity.this, ActivityDetailActivity.class) ;
                intent.putExtra("ainfo", activities) ;
                startActivity(intent);
            }
        });

    }

    private void setData(ArrayList<PieEntry> entries, boolean hasSuccess, boolean hasFail) {
        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setSliceSpace(3f);
        dataSet.setSelectionShift(5f);
        ArrayList<Integer> colors = new ArrayList<>();
        if (hasSuccess){
            colors.add(getResources().getColor(R.color.colorGreen));
        }
        if (hasFail){
            colors.add(getResources().getColor(R.color.colorRed)) ;
        }

        dataSet.setColors(colors);

        dataSet.setValueLinePart1OffsetPercentage(80.f);
        dataSet.setValueLinePart1Length(0.2f);
        dataSet.setValueLinePart2Length(0.4f);
        dataSet.setYValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);

        PieData data = new PieData(dataSet);
        data.setValueFormatter(new PercentFormatter());
        data.setValueTextSize(11f);
        data.setValueTextColor(Color.BLACK);
        mPieChart.setData(data);

        mPieChart.highlightValues(null);

        mPieChart.invalidate();
    }

    private SpannableString generateCenterSpannableText() {
        SpannableString s = new SpannableString(getString(R.string.result_summary_activity));
        return s;
    }

    private void renderViews() {
        //set title result
        String resultText = reportSummary.test_result ;
        String resultReason = reportSummary.test_reason ;
        resultTitleView.setText(resultText);
        resultTitleView.setTextColor(resultText.equals(getString(R.string.pass)) ?
                                    getResources().getColor(R.color.colorGreen) :
                                    getResources().getColor(R.color.colorRed));

        //set summary information
        ReportDetail detail = reportSummary.detail ;

        ReportDetail.SummaryInfo summary = detail.summaryInfo ;
        if (summary != null){
            summaryPkg.setText(summary.packageName);
            summaryTimeStamp.setText(summary.testTime);
            summaryDuration.setText(summary.testDuration);
            summaryCover.setText(summary.testConverage);
            summaryActivities.setText(reportSummary.totalActivities);
        }
        summaryFailReason.setText(resultReason != null ? resultReason : "N/A");

        //set activities pie chart
        ReportDetail.ActivityDetail activities = detail.activityInfo ;
        renderPieChart(activities, reportSummary.totalActivities);

        //set configurations
        ConfigInfo configInfo = detail.configInfo ;
//        configMaxSteps.setText(configInfo.sMaxSteps);
        configMaxRuntime.setText(configInfo.sMaxRuntime);
//        configAppRestartTimes.setText(configInfo.sMaxRestartAppTimes);
//        configLogMode.setText(configInfo.sLogMode);

        //set exception information
        final ReportDetail.ExceptionDetail exceptionDetail = detail.exceptionInfo ;
        if (exceptionDetail == null ||
                exceptionDetail.exceptionInfos == null || exceptionDetail.exceptionInfos.size() == 0){
            mTextAnr.setText(R.string.no_exception);
            mTextCrash.setText(R.string.no_exception);
            mExceptionInfo.setEnabled(false);
        }else{
            int crashCount = 0, anrCount = 0;
            List<ExceptionInfo> exceptions = exceptionDetail.exceptionInfos ;
            for (ExceptionInfo e : exceptions){
                if (e.isCrash){
                    crashCount ++ ;
                }else{
                    anrCount ++ ;
                }
            }

            mTextAnr.setText(anrCount == 0 ? getString(R.string.no_exception): anrCount +"");
            TextPaint tp = mTextAnr.getPaint();
            tp.setFakeBoldText(true);
            mTextAnr.setTextColor(getResources().getColor(R.color.colorRed));
            mTextCrash.setText(crashCount == 0 ? getString(R.string.no_exception): crashCount +"");
            TextPaint tp2 = mTextCrash.getPaint();
            tp2.setFakeBoldText(true);
            mTextCrash.setTextColor(getResources().getColor(R.color.colorRed));
            mExceptionInfo.setEnabled(true);
            mExceptionInfo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //close this, use dialog instead
/*                    Intent eIntent = new Intent(ResultActivity.this, ExceptionListActivity.class) ;
                    eIntent.putExtra("exception", exceptionDetail) ;
                    startActivity(eIntent);*/
                    AlertDialog.Builder builder = new AlertDialog.Builder(ResultActivity.this) ;
                    builder.setIcon(R.mipmap.ic_launcher)
                            .setCancelable(false)
                            .setTitle(R.string.exception_dialog_title)
                            .setMessage(R.string.exception_dialog_msg)
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                        //do nothing
                                }
                            }) ;
                    AlertDialog dialog = builder.create() ;
                    dialog.show();
                }
            });
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (convertTask != null){
            convertTask.cancel(true) ;
        }
    }
}
