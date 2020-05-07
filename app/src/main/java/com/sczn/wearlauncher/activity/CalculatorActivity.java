package com.sczn.wearlauncher.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.sczn.wearlauncher.R;
import com.sczn.wearlauncher.app.LogUtils;
import com.sczn.wearlauncher.base.AbsActivity;
import com.sczn.wearlauncher.util.DoubleUtil;

import java.util.regex.Pattern;

/**
 * Description:计算器
 * Created by Bingo on 2019/3/4.
 */
public class CalculatorActivity extends AbsActivity implements View.OnClickListener {
    private final String TAG = "CalculatorActivity";
    private String text="";
    private double count=0;
    private boolean isOperation=false;
    private boolean isOver=false;//等于之后就over
    private String tempnum="";
    TextView textView1,textView2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calculator_v2);
        textView1 = findViewById(R.id.display1);
        textView2 = findViewById(R.id.display2);
        findViewById(R.id.one).setOnClickListener(this);
        findViewById(R.id.two).setOnClickListener(this);
        findViewById(R.id.three).setOnClickListener(this);
        findViewById(R.id.four).setOnClickListener(this);
        findViewById(R.id.five).setOnClickListener(this);
        findViewById(R.id.six).setOnClickListener(this);
        findViewById(R.id.seven).setOnClickListener(this);
        findViewById(R.id.eight).setOnClickListener(this);
        findViewById(R.id.nine).setOnClickListener(this);
        findViewById(R.id.zero).setOnClickListener(this);
        findViewById(R.id.dot).setOnClickListener(this);
        findViewById(R.id.equal).setOnClickListener(this);
        findViewById(R.id.subtract).setOnClickListener(this);
        findViewById(R.id.add).setOnClickListener(this);
        findViewById(R.id.multiply).setOnClickListener(this);
        findViewById(R.id.divide).setOnClickListener(this);
        findViewById(R.id.back).setOnClickListener(this);
        findViewById(R.id.clear).setOnClickListener(this);
    }

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.one:
                addNum("1");
                setIsOperationTrue();
                break;
            case R.id.two:
                addNum("2");
                setIsOperationTrue();
                break;
            case R.id.three:
                addNum("3");
                setIsOperationTrue();
                break;
            case R.id.four:
                addNum("4");
                setIsOperationTrue();
                break;
            case R.id.five:
                addNum("5");
                setIsOperationTrue();
                break;
            case R.id.six:
                addNum("6");
                setIsOperationTrue();
                break;
            case R.id.seven:
                addNum("7");
                setIsOperationTrue();
                break;
            case R.id.eight:
                addNum("8");
                setIsOperationTrue();
                break;
            case R.id.nine:
                addNum("9");
                setIsOperationTrue();
                break;
            case R.id.zero:
                addNum("0");
                setIsOperationTrue();
                break;
            case R.id.dot:
                addNum(".");
                break;
            case R.id.equal:
                equal();
                break;
            case R.id.subtract:
                subtract();
                break;
            case R.id.add:
                add();
                break;
            case R.id.multiply:
                multiply();
                break;
            case R.id.divide:
                divide();
                break;
            case R.id.back:
                back();
                break;
            case R.id.clear:
                clear();
                break;
        }
    }
    private void setIsOperationTrue() {
        isOperation=true;
    }
    private void setIsOperationFalse() {
        isOperation=false;
    }
    private void setMyTextView1(String str) {
        textView1.setText(str);
    }
    private void setMyTextView2(String str) {
        textView2.setText(str);
    }


    /**
     * 添加一个数字
     * @param str
     */
    private void addNum(String str) {
        //防止.1小数点开头
        if (str.equals(".") && tempnum.equals("")) {
            return;
        }
        //防止出现多个小数点
        if (str.equals(".") &&tempnum.contains(".")) {
            return;
        }
        isOver=false;
        tempnum+=str;
        setMyTextView1(text+tempnum);
    }
    private void clear(){//清除
        clear2();
        setMyTextView1("");
        setMyTextView2("");
    }
    private void clear2(){
        lastSign="";
//    	isOver=false;
        tempnum="";
        setIsOperationFalse();
        count=0;
        text="";
    }

    private void back(){//后退
        if(!isOver){
            try{
                tempnum=tempnum.substring(0, (tempnum.length()-1));
                setMyTextView1(text+tempnum);
            }catch (Exception e) {
                e.printStackTrace();
                LogUtils.e(TAG, e.getMessage());
            }
        }
    }
    private void equal() {//等于
        if(!isOver){
            isOver=true;//等于之后就over
            jisuan();
            text+=tempnum+"=";
            setMyTextView1(text);
            setMyTextView2(""+count);
            clear2();
        }
    }

    private String lastSign="";
    private void jisuan() {
        if (verifyNumber(tempnum)) {
            Toast.makeText(this, "输入的内容不是数字，请重新输入", Toast.LENGTH_SHORT).show();
            return;
        }

        double tem=Double.parseDouble(tempnum);
        double result = 0;
        if("+".equals(lastSign)){
            result= DoubleUtil.add(count, tem);
        }else if("-".equals(lastSign)){
            result = DoubleUtil.sub(count, tem);
        }else if("x".equals(lastSign)){
            result = DoubleUtil.mul(count, tem);
        }else if("÷".equals(lastSign)){
            result = DoubleUtil.divide(count, tem, 8);
        }else if("".equals(lastSign)){
            result = tem;
        }
    	LogUtils.d(TAG, "计算:"+count+lastSign+tem+"="+result);
        count = result;

    }

    /**
     * 加法
     */
    private void add() {
        if(!isOver){
            jisuan();
            lastSign="+";
            setIsOperationFalse();
            isOver=true;
            text+=tempnum+"+";
            tempnum="";
            setMyTextView1(text);
        }
    }

    /**
     * 减法
     */
    private void subtract() {
        if(!isOver){
            jisuan();
            lastSign="-";
            setIsOperationFalse();
            isOver=true;
            text+=tempnum+"-";
            tempnum="";
            setMyTextView1(text);
        }
    }

    /**
     * 乘法
     */
    private void multiply() {
        if(!isOver){
            jisuan();
            lastSign="x";
            setIsOperationFalse();
            isOver=true;
            text+=tempnum+"x";
            tempnum="";
            setMyTextView1(text);
        }
    }

    /**
     * 除法
     */
    private void divide() {
        if(!isOver){
            jisuan();
            lastSign="÷";
            setIsOperationFalse();
            isOver=true;
            text+=tempnum+"÷";
            tempnum="";
            setMyTextView1(text);
        }
    }

     /**
      *  检查是否为数字
      * @param input
      * @return
      */
    private boolean verifyNumber(String input) {
        Pattern pattern = Pattern.compile("^[-+]?(([0-9]+)([.]([0-9]+))?|([.]([0-9]+))?)$");
        return input.equals("") || !pattern.matcher(input).matches();
    }



   /* *//**
     * 数字按钮
     *
     * @param view
     *//*
    public void numberButtonClick(View view) {
        TextView button = (TextView) view;
        if (operation == Operation.STARTOVER) {
            display.setText("");
            operation = Operation.NONE;
        }
        //防止.1小数点开头
        if (button.getText().toString().equals(".") && display.getText().toString().equals("")) {
            return;
        }
        //防止出现多个小数点
        if (button.getText().toString().equals(".") && display.getText().toString().contains(".")) {
            return;
        }
        display.append(button.getText().toString());
    }

    *//**
     * 等于=的按钮操作
     *
     * @param view
     *//*
    public void equalButtonClick(View view) {
        String input = display.getText().toString().trim();
        LogUtils.d(TAG, "等于=的按钮操作:" + input);
        if (verifyNumber(input)) {
            return;
        }
        double secondNumber = Double.parseDouble(input);
        switch (operation) {
            case ADD:
                firstNumber = DoubleUtil.add(firstNumber, secondNumber);
                break;
            case SUBTRACT:
                firstNumber = DoubleUtil.sub(firstNumber, secondNumber);
                break;
            case MULTIPLY:
                firstNumber = DoubleUtil.mul(firstNumber, secondNumber);
                break;
            case DIVIDE:
                firstNumber = DoubleUtil.divide(firstNumber, secondNumber, 8);
                break;
            default:
                firstNumber = secondNumber;
        }
        display.setText(getPrettyNumber(firstNumber));
        operation = Operation.STARTOVER;
    }



    *//**
     * 去掉多余的0
     *
     * @param number
     * @return
     *//*
    private String getPrettyNumber(double number) {
        //return BigDecimal.valueOf(number)
        //        .stripTrailingZeros().toPlainString();
        LogUtils.i(TAG, "" + number);
        return subZeroAndDot(String.valueOf(number));
    }

    *//**
     * 使用java正则表达式去掉多余的.与0
     *
     * @param s
     * @return
     *//*
    public static String subZeroAndDot(String s) {
        if (s.indexOf(".") > 0) {
            s = s.replaceAll("0+?$", "");//去掉多余的0
            s = s.replaceAll("[.]$", "");//如最后一位是.则去掉
        }


        return s;
    }*/


}
