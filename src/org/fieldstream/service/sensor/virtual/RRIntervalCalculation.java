package org.fieldstream.service.sensor.virtual;

import java.util.Arrays;

public class RRIntervalCalculation{
    
    private static final int L=5000;
    public int peaktimes[]=new int[L];
    public int pind=0;
    
    public RRIntervalCalculation(){}
    
    private int RR_ptr = 0;
    int lastPeakTime = 0;
    private float[] RR = new float[L];
    
    public int[] tbody;
    public int[] tbandbody0;
    public float[] tdiffbody0;
    public float[] tlpbody0;
    public float[] tlpbody1;
    
    // ==============================================================
    public int[] calculate(int[] databuffer,long[] timestamps){
        // =============================================================
        int count=0;
        for(int i=0;i<databuffer.length;i++) if(databuffer[i]==-1) count++;
        int[] empty={};
        if(count>0.8*databuffer.length) return empty;
        // =============================================================
        boolean datasegment=true;
        int segmentstart=0;
        float[] RR={};
        if(databuffer[0]==-1) datasegment=false;
        // =============================================================
        for(int i=0;i<databuffer.length;i++){
            if(!datasegment){
                if(databuffer[i]!=-1){
                    segmentstart=i;
                    datasegment=true;
                }
            }else{
                if(databuffer[i]==-1){
                    int segmentlength=i-segmentstart;
                    if(segmentlength>64*10){
                        int[] newbuff=new int[segmentlength];
                        int k=0;
                        for(int j=segmentstart;j<i;j++) newbuff[k++]=databuffer[j];
                        RR=concat(RR,calculateSegment(newbuff));
                    }
                    datasegment=false;
                }
                if(i==databuffer.length-1){
                    int segmentlength=databuffer.length-segmentstart;
                    if(segmentlength>64*10){
                        int[] newbuff=new int[segmentlength];
                        int k=0;
                        for(int j=segmentstart;j<databuffer.length;j++) newbuff[k++]=databuffer[j];
                        RR=concat(RR,calculateSegment(newbuff));
                    }
                }
            }
        }
        // =============================================================
        if(RR.length<20){
            return empty;
        }else{
        	int[] returnRR=new int[RR.length];
        	for(int i=0;i<RR.length;i++) returnRR[i]=(int)RR[i];
            return returnRR;
        }
    }
    // ==============================================================
    
    public float[] concat(float[] a, float[] b){
        float[] out=new float[a.length+b.length];
        int k=0;
        for(int i=0;i<a.length;i++) out[k++]=a[i];
        for(int i=0;i<b.length;i++) out[k++]=b[i];
        return out;
    }
    
    public float[] calculateSegment(int[] databuffer) {
        int len = databuffer.length;
       
        tbody=new int[len];
        tbandbody0 = new int[len];
        tdiffbody0 = new float[len];
        tlpbody0 = new float[len];
        
        tbody = lowpass(databuffer);
        tbandbody0 = bandpass(tbody);
        
        float tbandbody00[]=new float[len];
        for(int i=0;i<len;i++) tbandbody00[i]=(float)tbandbody0[i];
        
        tdiffbody0 = diffsqfilt(tbandbody00);
        tlpbody0 = lpfilt(tdiffbody0);
        tlpbody1=removelargepeak(tlpbody0);
        
        //=======================================
        
        float tlpbody00[]=new float[L];
        for(int i=0;i<len;i++) tlpbody00[i]=(float)tlpbody1[i];
        RR = findpks(tlpbody00);
        
        float[] return_RR = new float[RR_ptr];
        System.arraycopy(RR, 0, return_RR, 0, RR_ptr);
        
        Arrays.fill(RR, 0, RR_ptr, 0);
        RR_ptr = 0;
        
        return return_RR;
    }

    // ==============================================================
    public float[] removelargepeak(float[] v){
        int len=v.length;
        int len2=64*2;
        float a=v[len2];
        float b=v[len2];
        float alpha=(float)0.1;
        float[] out=new float[len];
        
        for(int i=len2;i<len;i++){
            b=(v[i]>b)?v[i]:b;
            a=(v[i]<a)?v[i]:a;
        }
        
        for(int i=0;i<len;i++){
//            if(v[i]>maxval) out[i]=maxval;
//            else if(v[i]<minval) out[i]=minval;
//            else out[i]=v[i];
            if(v[i]>=a && v[i]<=b){
                out[i]=v[i];
            }else if(v[i]<a){
                out[i]=(float)a/(1+alpha*(v[i]-a)*(v[i]-a));
            }else{
                out[i]=a+(float)1.1*(b-a)-b/(1+alpha*(v[i]-b)*(v[i]-b));
            }
            
        
        }
        return out;
    }

    // ==============================================================
   
    public float[] findpks(float[] tlpbody02) {
        final float theta=(float) 0.125;
        final float RR_high_limit = (float) 1.4;
        final float RR_low_limit = (float) 0.6;
        final float RR_missed_limit = (float) 1.6;
        final int min_distance = 25;
        float SPK = 300;
        float NPK = 60;
        float THR1 = 150;
        float THR2 = 75;
        int countq = 0;
        int x;
        float curPeak;
        int temp1 = 0;
        int RR_sel_ptr = 0;
        float RRAVERAGE_1 = 45;
        int[] RR_SEL = new int[8];
        int SIZE = tlpbody02.length;
        int[] pks = new int[SIZE];
        float[] valuepks = new float[SIZE];
        int count = 0;
        
        // ======================================
        // FINDS THE PEAKS
        // ======================================
        for (int j = 2; j < SIZE - 2; j++) {
            if (tlpbody02[j - 2] < tlpbody02[j - 1]
            && tlpbody02[j - 1] < tlpbody02[j]
            && tlpbody02[j] >= tlpbody02[j + 1]
            && tlpbody02[j + 1] > tlpbody02[j + 2]) {
                pks[count] = j - 1;
                valuepks[count] = tlpbody02[j];
                count++;
            }
        }
        
        // ======================================
        // FINDS R-PEAKS
        // ======================================
        for (int j = 0; j < count; j++) {
            x = pks[j];
            curPeak = valuepks[j];
            if (curPeak > THR1 && x - lastPeakTime > min_distance) {
                SPK = (float) (curPeak * theta + SPK * (1-theta));
                RR[RR_ptr] = pks[j] - lastPeakTime;
                // out.println(RR[RR_ptr]);
                if (RR[RR_ptr] <= RRAVERAGE_1 * RR_high_limit
                && RR[RR_ptr] >= RRAVERAGE_1 * RR_low_limit) {
                    RR_SEL[RR_sel_ptr] = (int) RR[RR_ptr];
                    RR_sel_ptr = RR_sel_ptr + 1;
                    RR_ptr = RR_ptr + 1;
                    if (RR_sel_ptr == 8) {
                        RR_sel_ptr = 0;
                        for (int m = 0; m < 8; m++) {
                            temp1 = temp1 + RR_SEL[m];
                        }
                        RRAVERAGE_1 = (float) (temp1 * 0.125);
                    }
                    temp1 = 0;
                }
                lastPeakTime = x;
//                peaktimes[pind++]=x;
                countq = countq + 1;
            } else if (pks[j] - lastPeakTime > RRAVERAGE_1 * RR_missed_limit) {
                for (int k = 1; k < j; k++) {
                    curPeak = valuepks[k];
                    if (curPeak > SPK * 0.25) {
                        if (curPeak > THR2
                        && (pks[k] - lastPeakTime > min_distance)) {
                            countq = countq + 1;
                            SPK = (float) (theta * curPeak + (1-theta) * SPK);
                            RR[RR_ptr] = pks[k] - lastPeakTime;
                            lastPeakTime = pks[k];
                            //peaktimes[pind++]=pks[k];
                            if (RR[RR_ptr] <= RRAVERAGE_1 * RR_high_limit
                            && RR[RR_ptr] >= RRAVERAGE_1 * RR_low_limit) {
                                RR_SEL[RR_sel_ptr] = (int) RR[RR_ptr];
                                RR_sel_ptr = RR_sel_ptr + 1;
                                RR_ptr = RR_ptr + 1;
                                if (RR_sel_ptr == 8) {
                                    RR_sel_ptr = 0;
                                    for (int m = 0; m < 8; m++) {
                                        temp1 = temp1 + RR_SEL[m];
                                    }
                                    RRAVERAGE_1 = (float) (temp1 * 0.125);
                                }
                                temp1 = 0;
                            }
                        }
                    }
                }
            } else {
                NPK = (float) (curPeak * theta + NPK * (1-theta));
            }
            THR1 = (float) (NPK + (SPK - NPK) * 0.25);
            THR2 = THR1 / 2;
        }
        lastPeakTime = lastPeakTime - SIZE;
        return RR;
    }
    
    // First low pass filter
    public int[] lowpass(int[] data) {
        int k = 0;
        int SIZE = data.length;
        int[] x0 = new int[4];
        int[] y0 = new int[4];
        for (int i = 0; i < SIZE; i++) {
            x0[k] = data[i];
            y0[k] = B0 * x0[k] + B1 * x0[(k + 3) % FILTER_SIZE] + B2
            * x0[(k + 2) % FILTER_SIZE] + B3
            * x0[(k + 1) % FILTER_SIZE] - A1
            * y0[(k + 3) % FILTER_SIZE] - A2
            * y0[(k + 2) % FILTER_SIZE] - A3
            * y0[(k + 1) % FILTER_SIZE];
            y0[k] = (y0[k]) >> FILTER_GAIN_REMOVE;
            if (y0[k] > 0) {
                data[i] = y0[k];
            } else {
                data[i] = 2000;
            }
            k = (k + 1) % FILTER_SIZE;
        }
        return data;
    }
    
    // Band Pass filter
    public int[] bandpass(int[] data) {
        int k1 = 0;
        int SIZE = data.length;
        int[] x1 = new int[5];
        int[] y1 = new int[5];
        for (int i = 0; i < SIZE; i++) {
            x1[k1] = data[i];
            y1[k1] = BB0 * x1[k1] + BB1 * x1[(k1 + 4) % FILTER_SIZE_BAND] + BB2
            * x1[(k1 + 3) % FILTER_SIZE_BAND] + BB3
            * x1[(k1 + 2) % FILTER_SIZE_BAND] + BB4
            * x1[(k1 + 1) % FILTER_SIZE_BAND] - AA1
            * y1[(k1 + 4) % FILTER_SIZE_BAND] - AA2
            * y1[(k1 + 3) % FILTER_SIZE_BAND] - AA3
            * y1[(k1 + 2) % FILTER_SIZE_BAND] - AA4
            * y1[(k1 + 1) % FILTER_SIZE_BAND];
            
            y1[k1] = (y1[k1]) >> FILTER_GAIN_REMOVE_BAND;
            if (y1[k1] == 0) {
                data[i] = 0;
            } else {
                data[i] = y1[k1] + 1500;
            }
            k1 = (k1 + 1) % FILTER_SIZE_BAND;
        }
        return data;
    }
    
    // Derivative filter and squaring
    public float[] diffsqfilt(float[] data) {
        float[] x2 = new float[5];
        float[] y2 = new float[5];
        int k2 = 0;
        final int SQ = 12;
        int SIZE = data.length;
        for (int i = 0; i < SIZE; i++) {
            x2[k2] = data[i];
            y2[k2] = BBD0 * x2[k2] + BBD1 * x2[(k2 + 4) % FILTER_SIZE_DIFF]
            + BBD2 * x2[(k2 + 3) % FILTER_SIZE_DIFF] + BBD3
            * x2[(k2 + 2) % FILTER_SIZE_DIFF] + BBD4
            * x2[(k2 + 1) % FILTER_SIZE_DIFF];
            y2[k2] = (y2[k2]) / (float)Math.pow(2,FILTER_GAIN_REMOVE_DIFF);
            data[i] = y2[k2];
            //if (data[i] > -4096 && data[i] < 4096) {
            data[i] = data[i] * data[i];
            data[i] = data[i] / (float)Math.pow(2,SQ);
            //}
            
            k2 = (k2 + 1) % FILTER_SIZE_DIFF;
        }
        return data;
    }
    
    // Final Low Pass filter2
    public float[] lpfilt(float[] data) {
        float[] x3 = new float[26];
        float[] y3 = new float[26];
        int k3 = 0;
        int SIZE = data.length;
        for (int i = 0; i < SIZE; i++) {
            x3[k3] = data[i];
            
            y3[k3] = AAL0 * x3[k3] + AAL1 * x3[(k3 + 25) % FILTER_SIZE_LP]
            + AAL2 * x3[(k3 + 24) % FILTER_SIZE_LP]
            + AAL3 * x3[(k3 + 23) % FILTER_SIZE_LP]
            + AAL4 * x3[(k3 + 22) % FILTER_SIZE_LP]
            + AAL5 * x3[(k3 + 21) % FILTER_SIZE_LP]
            + AAL6 * x3[(k3 + 20) % FILTER_SIZE_LP]
            + AAL7 * x3[(k3 + 19) % FILTER_SIZE_LP]
            + AAL8 * x3[(k3 + 18) % FILTER_SIZE_LP]
            + AAL9 * x3[(k3 + 17) % FILTER_SIZE_LP]
            + AAL10 * x3[(k3 + 16) % FILTER_SIZE_LP]
            + AAL11 * x3[(k3 + 15) % FILTER_SIZE_LP]
            + AAL12 * x3[(k3 + 14) % FILTER_SIZE_LP]
            + AAL13 * x3[(k3 + 13) % FILTER_SIZE_LP]
            + AAL14 * x3[(k3 + 12) % FILTER_SIZE_LP]
            + AAL15 * x3[(k3 + 11) % FILTER_SIZE_LP]
            + AAL16 * x3[(k3 + 10) % FILTER_SIZE_LP]
            + AAL17 * x3[(k3 + 9) % FILTER_SIZE_LP]
            + AAL18 * x3[(k3 + 8) % FILTER_SIZE_LP]
            + AAL19 * x3[(k3 + 7) % FILTER_SIZE_LP]
            + AAL20 * x3[(k3 + 6) % FILTER_SIZE_LP]
            + AAL21 * x3[(k3 + 5) % FILTER_SIZE_LP] + AAL22
            * x3[(k3 + 4) % FILTER_SIZE_LP] + AAL23
            * x3[(k3 + 3) % FILTER_SIZE_LP] + AAL24
            * x3[(k3 + 2) % FILTER_SIZE_LP] + AAL25
            * x3[(k3 + 1) % FILTER_SIZE_LP];
            
            y3[k3] = (y3[k3]) /(float)Math.pow(2,FILTER_GAIN_REMOVE_LP);
            if (y3[k3] > -500) {
                data[i] = y3[k3];
            } else {
                data[i] = 0;
            }
            k3 = (k3 + 1) % FILTER_SIZE_LP;
        }
        return data;
    }
    
    private static final int FILTER_GAIN_REMOVE = 16; // Right shift by X
    private static final int A1 = -8208;
    private static final int A2 = 5927;
    private static final int A3 = -1482;
    private static final int B0 = 4096;
    private static final int B1 = 12288;
    private static final int B2 = 12288;
    private static final int B3 = 4096;
    private static final int FILTER_SIZE = 4;
    
    private static final int FILTER_GAIN_REMOVE_BAND = 21; // Right shift by X
    private static final int BB0 = 1048576;
    private static final int BB1 = -1593350;
    private static final int BB2 = 1361436;
    private static final int BB3 = -826591;
    private static final int BB4 = -826591;
    private static final int AA1 = 0;
    private static final int AA2 = -343174;
    private static final int AA3 = 0;
    private static final int AA4 = 0;
    private static final int FILTER_SIZE_BAND = 5;
    
    private static final int FILTER_GAIN_REMOVE_DIFF = 2; // Right shift by X
    private static final int BBD0 = -69;
    private static final int BBD1 = 149;
    private static final int BBD2 = 0;
    private static final int BBD3 = -147;
    private static final int BBD4 = 69;
    private static final int FILTER_SIZE_DIFF = 5;
    
    private static final int FILTER_GAIN_REMOVE_LP = 20;
    private static final int AAL0 = -678;
    private static final int AAL1 = -2916;
    private static final int AAL2 = -7628;
    private static final int AAL3 = -15015;
    private static final int AAL4 = -23327;
    private static final int AAL5 = -28634;
    private static final int AAL6 = -25219;
    private static final int AAL7 = -7737;
    private static final int AAL8 = 26249;
    private static final int AAL9 = 73875;
    private static final int AAL10 = 126634;
    private static final int AAL11 = 172268;
    private static final int AAL12 = 198793;
    private static final int AAL13 = 198793;
    private static final int AAL14 = 172268;
    private static final int AAL15 = 126634;
    private static final int AAL16 = 73875;
    private static final int AAL17 = 26249;
    private static final int AAL18 = -7737;
    private static final int AAL19 = -25219;
    private static final int AAL20 = -28634;
    private static final int AAL21 = -23327;
    private static final int AAL22 = -15015;
    private static final int AAL23 = -7628;
    private static final int AAL24 = -2916;
    private static final int AAL25 = -678;
    private static final int FILTER_SIZE_LP = 26;
    
}