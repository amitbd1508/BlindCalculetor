package com.blackflag.blindcalculetor;

import android.content.Intent;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import java.text.DecimalFormat;
import java.util.LinkedList;

public class MainActivity extends AppCompatActivity {


    private Speaker speaker;
    public static final String ADD = "\u002B";
    public static final String SUB = "\u2212";
    public static final String DIV = "\u00F7";
    public static final String MUL = "\u2715";
    public String value = "";
    private final int CHECK_CODE = 0x1;
    public LinkedList<String> operators = new LinkedList<String>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        MobileAds.initialize(getApplicationContext(), "ca-app-pub-6451736138557147~5250212717");

        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
        checkTTS();


    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        speaker.destroy();
    }

    private void checkTTS(){
        Intent check = new Intent();
        check.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(check, CHECK_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == CHECK_CODE){
            if(resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS){
                speaker = new Speaker(this);
                Log.v("====",requestCode+"="+CHECK_CODE);
            }else {
                Intent install = new Intent();
                install.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                startActivity(install);
            }
        }

    }

    public void registerKey(View view)
    {

        switch(view.getId())
        {

            case R.id.button0:
                safelyPlaceOperand("0");
                break;
            case R.id.button1:
                safelyPlaceOperand("1");
                break;
            case R.id.button2:
                safelyPlaceOperand("2");
                break;
            case R.id.button3:
                safelyPlaceOperand("3");
                break;
            case R.id.button4:
                safelyPlaceOperand("4");
                break;
            case R.id.button5:
                safelyPlaceOperand("5");
                break;
            case R.id.button6:
                safelyPlaceOperand("6");
                break;
            case R.id.button7:
                safelyPlaceOperand("7");
                break;
            case R.id.button8:
                safelyPlaceOperand("8");
                break;
            case R.id.button9:
                safelyPlaceOperand("9");
                break;
            case R.id.buttonAdd:
                safelyPlaceOperator(ADD);
                break;
            case R.id.buttonSub:
                safelyPlaceOperator(SUB);
                break;
            case R.id.buttonDiv:
                safelyPlaceOperator(DIV);
                break;
            case R.id.buttonMul:
                safelyPlaceOperator(MUL);
                break;
            case R.id.buttonDel:
                deleteFromLeft();
                break;
        }
        display();
    }

    private void display()
    {
        TextView tvAns = (TextView) findViewById(R.id.textViewAns);
        tvAns.setText(value);
        speaker.speak(value);
    }

    private void display(String s)
    {
        TextView tvAns = (TextView) findViewById(R.id.textViewAns);
        tvAns.setText(s);
    }

    private void safelyPlaceOperand(String op)
    {
        speaker.speak(op+" Clicked");
        int operator_idx = findLastOperator();

        if (operator_idx != value.length()-1 && value.charAt(operator_idx+1) == '0')
            deleteTrailingZero();
        value += op;
    }

    private void safelyPlaceOperator(String op)
    {
        if (endsWithOperator())
        {
            deleteFromLeft();
            value += op;
            operators.add(op);
        }
        else if (endsWithNumber())  // Safe to place operator right away.
        {
            value += op;
            operators.add(op);
        }

    }

    private void deleteTrailingZero()
    {
        if (value.endsWith("0")) deleteFromLeft();
    }

    private void deleteFromLeft()
    {
        if (value.length() > 0)
        {
            if (endsWithOperator()) operators.removeLast();
            value = value.substring(0, value.length()-1);
        }
    }

    private boolean endsWithNumber()
    {

        return value.length() > 0 && Character.isDigit(value.charAt(value.length()-1));
    }

    private boolean endsWithOperator()
    {
        if (value.endsWith(ADD) || value.endsWith(SUB) || value.endsWith(MUL) || value.endsWith(DIV)) return true;
        else return false;
    }

    private int findLastOperator()
    {
        int add_idx = value.lastIndexOf(ADD);
        int sub_idx = value.lastIndexOf(SUB);
        int mul_idx = value.lastIndexOf(MUL);
        int div_idx = value.lastIndexOf(DIV);
        return Math.max(add_idx, Math.max(sub_idx, Math.max(mul_idx, div_idx)));
    }

    public void calculate(View view)
    {
        if (operators.isEmpty()) return;
        if (endsWithOperator())
        {
            display("incorrect format");
            resetCalculator();
            return;
        }


        String[] operands = value.split("\\u002B|\\u2212|\\u00F7|\\u2715");

        int i = 0;
        double ans = Double.parseDouble(operands[i]);
        for (String operator : operators)
            ans = applyOperation(operator, ans, Double.parseDouble(operands[++i]));

        DecimalFormat df = new DecimalFormat("0.###");
        display(df.format(ans));
        resetCalculator();

    }

    private double applyOperation(String operator, double operand1, double operand2)
    {

        if (operator.equals(ADD)) return operand1 + operand2;
        if (operator.equals(SUB)) return operand1 - operand2;
        if (operator.equals(MUL)) return operand1 * operand2;
        if (operator.equals(DIV)) return operand1 / operand2;
        return 0.0;
    }

    private void resetCalculator()
    {
        value = "";
        operators.clear();
    }
}
