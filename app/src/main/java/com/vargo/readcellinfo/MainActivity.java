package com.vargo.readcellinfo;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Environment;
import android.os.PowerManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.CellIdentityCdma;
import android.telephony.CellIdentityGsm;
import android.telephony.CellIdentityLte;
import android.telephony.CellIdentityWcdma;
import android.telephony.CellInfo;
import android.telephony.CellInfoCdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.CellLocation;
import android.telephony.CellSignalStrengthCdma;
import android.telephony.CellSignalStrengthGsm;
import android.telephony.CellSignalStrengthLte;
import android.telephony.CellSignalStrengthWcdma;
import android.telephony.NeighboringCellInfo;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "zzzMainActivity";
    private TextView tvDemo1;
    private Button btnDemo1;
    private Button btnDemo2;
    private Button btnDemo3;
    private CellRecord mRecord = new CellRecord();
    private PowerManager.WakeLock mLock;


    private PhoneStateListener phoneStateListener = new PhoneStateListener(){
        @Override
        public void onCellLocationChanged(CellLocation location){
            Log.d(TAG, "onCellLocationChanged "+encodeCellLocation(location));
        }
        @Override
        public void onSignalStrengthsChanged(SignalStrength signalStrength) {
            Log.d(TAG, "onSignalStrengthsChanged "+signalStrength);
        }
        @Override
        public void onCellInfoChanged(List<CellInfo> cellInfo) {
            Log.d(TAG, "onCellInfoChanged "+encodeCellInfo(cellInfo));
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvDemo1 = (TextView)findViewById(R.id.tvDemo1);
        btnDemo1 = (Button)findViewById(R.id.btnDemo1);
        btnDemo2 = (Button)findViewById(R.id.btnDemo2);
        btnDemo3 = (Button)findViewById(R.id.btnDemo3);
        btnDemo1.setOnClickListener(this);
        btnDemo2.setOnClickListener(this);
        btnDemo3.setOnClickListener(this);
        if (isGrantAllNeededPermissions()){
            btnDemo1.setEnabled(true);
            btnDemo2.setEnabled(true);
            btnDemo3.setEnabled(true);
            initCellListener();

        }else {
            btnDemo1.setEnabled(false);
            btnDemo2.setEnabled(false);
            btnDemo3.setEnabled(false);
            PermissionUtil.requestAllPermissions(this);
        }
        VendorDatabase.init(this);


    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (isGrantAllNeededPermissions()) {
            btnDemo1.setEnabled(true);
            btnDemo2.setEnabled(true);
            btnDemo3.setEnabled(true);

            initCellListener();
            refreshRecord();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isGrantAllNeededPermissions()) {
            refreshRecord();
        }

    }

    private boolean isGrantAllNeededPermissions() {
        return PermissionUtil.isGrantAccessCoarseLocationPermission(this) &&
                PermissionUtil.isGrantWriteExternalStoragePermission(this);

    }
    private void refreshRecord() {
        Log.d(TAG, "refreshRecord");
        encodeCellInfo();
        if(TextUtils.isEmpty(mRecord.location)){
            Toast.makeText(this, "location is empty!", Toast.LENGTH_SHORT).show();
        } else if(VendorDatabase.isCellExist(mRecord.location)){
            Toast.makeText(this, "location already saved!", Toast.LENGTH_SHORT).show();
        }else {
            boolean b = VendorDatabase.insertCell(mRecord);
            Toast.makeText(this, "location saved "+(b?"success!":"failed!"), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onClick(View v) {
        if(v == btnDemo1) { // write
            refreshRecord();
        }else if(v == btnDemo2) {
            Intent intent = new Intent(this, RecordsActivity.class);
            startActivity(intent);
        } else if(v == btnDemo3) {
            long count = VendorDatabase.getCellsCount();
            if(count == 0){
                Toast.makeText(this, "total count is 0!", Toast.LENGTH_SHORT).show();
            }else {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
                String dir = Environment.getExternalStorageDirectory().getAbsolutePath()+"/ReadCellInfo";
                String filepath = dir+"/cells_"+sdf.format(new Date())+".txt";
                try {
                    File file = new File(dir);
                    if (file.exists()&&!file.isDirectory()) file.delete();
                    else file.mkdirs();
                    file = new File(filepath);
                    if (file.exists()) file.delete();

                    file.createNewFile();
                    FileOutputStream fos = new FileOutputStream(file);
                    int writes = 0;
                    for (int i = 0; i < count; i++) {
                        CellRecord r = VendorDatabase.getCellByIndex(i);
                        if(r != null){
                            writes ++;
                            fos.write((r.toString()+"\n").getBytes());
                        }
                    }
                    fos.close();
                    Toast.makeText(this, "Dumped "+writes+" records!", Toast.LENGTH_SHORT).show();
                }catch (Exception e){}
            }
        }
    }

    private void encodeCellInfo() {
        Log.d(TAG, "encodeCellInfo");
        try {
            TelephonyManager mgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            mRecord.location = encodeCellLocation(mgr.getCellLocation());
            mRecord.cells =encodeCellInfo(mgr.getAllCellInfo());
            mRecord.neighbors = encodeNeighboringCellInfo(mgr.getNeighboringCellInfo());
            StringBuilder sb = new StringBuilder();
            sb.append("Location:\n").append(mRecord.location).append("\n");
            sb.append("Cells:\n").append(mRecord.cells).append("\n");
            sb.append("Neighbors:\n").append(mRecord.neighbors).append("\n");
            tvDemo1.setText(sb.toString());
        }catch (Exception e){


        }
    }
    private String encodeNeighboringCellInfo(List<NeighboringCellInfo> list){
        if(list == null || list.size() == 0) return null;
        String s = "[";
        for(NeighboringCellInfo ce: list){
            String ss = encodeNeighboringCellInfo(ce);
            if(ss != null){
                s+="{"+ss+"},";
            }
        }
        if(s.length()>0){
            s = s.substring(0, s.length()-1);
        }
        s+="]";
        return s;
    }
    private String encodeNeighboringCellInfo(NeighboringCellInfo info){
        if(info == null) return null;
        return String.format("%d:%d:%d:%d:%d", info.getNetworkType(),info.getLac(), info.getCid(), info.getPsc(), info.getRssi());
    }
    private String encodeCellLocation(CellLocation loc){
        if(loc == null) return null;
        if(loc instanceof GsmCellLocation){
            //1:lac:cid:psc
            GsmCellLocation g= (GsmCellLocation)loc;
            return String.format("1:%d:%d:%d", g.getLac(), g.getCid(),
                    g.getPsc());
        }else if(loc instanceof CdmaCellLocation){
            //2:network_id:bs_id:sys_id:lat:lng
            CdmaCellLocation g= (CdmaCellLocation)loc;
            return String.format("2:%d:%d:%d:%d:%d", g.getNetworkId(), g.getBaseStationId(),
                    g.getSystemId(), g.getBaseStationLatitude(), g.getBaseStationLongitude());
        }
        return null;

    }
    private String encodeCellInfo(List<CellInfo> list){
        if(list == null || list.size() == 0) return null;
        String s = "[";
        for(CellInfo ce: list){
            String ss = encodeCellInfo(ce);
            if(ss != null){
                s+="{"+ss+"},";
            }
        }
        if(s.length()>0){
            s = s.substring(0, s.length()-1);
        }
        s+="]";
        return s;
    }
    private String encodeCellInfo(CellInfo ce){
        if(ce == null) return null;
        if(ce instanceof CellInfoGsm){
            //1(lac:cid:mcc:mnc:bsic:arfcn)(ss:ber:ta)
            CellInfoGsm i = (CellInfoGsm)ce;
            if(i != null){
                String s = "1(";
                CellIdentityGsm g = i.getCellIdentity();
                if(g != null){
                    s += String.format("%d:%d:%d:%d",
                            g.getLac()==Integer.MAX_VALUE?-1:g.getLac(),
                            g.getCid()==Integer.MAX_VALUE?-1:g.getCid(),
                            g.getMcc()==Integer.MAX_VALUE?-1:g.getMcc(),
                            g.getMnc()==Integer.MAX_VALUE?-1:g.getMnc());
                    if(Build.VERSION.SDK_INT > Build.VERSION_CODES.N){
                        s+=String.format(":%d:%d",
                                g.getBsic()==Integer.MAX_VALUE?-1:g.getBsic(),
                                g.getArfcn()==Integer.MAX_VALUE?-1:g.getArfcn());
                    }
                }
                s+=")(";
                CellSignalStrengthGsm n = i.getCellSignalStrength();
                if(n != null){
                    /*"CellSignalStrengthGsm:"
                            + " ss=" + mSignalStrength
                            + " ber=" + mBitErrorRate
                            + " mTa=" + mTimingAdvance;*/

                    String[] arrs = n.toString().split(" ");
                    if(arrs != null) {
                        try {
                            int ss = -1;
                            int ber = -1;
                            int ta = -1;
                            for (String a : arrs) {
                                if (a.startsWith("ss=")) {
                                    ss = Integer.parseInt(a.substring(3));
                                } else if (a.startsWith("ber=")) {
                                    ber = Integer.parseInt(a.substring(4));
                                } else if (a.startsWith("mTa=")) {
                                    ta = Integer.parseInt(a.substring(4));
                                }
                            }
                            if(ss == Integer.MAX_VALUE) ss = -1;
                            if(ber == Integer.MAX_VALUE) ber = -1;
                            if(ta == Integer.MAX_VALUE) ta = -1;
                            s += String.format("%d:%d:%d", ss, ber, ta);
                        }catch (Exception e2){}
                    }
                }
                s+=")";
                return s;
            }
        }else if(ce instanceof CellInfoCdma){
            //2(network_id:bs_id:sys_id:lat:lng)(cmd_dbm:cdma_ecio:evdo_dbm:evdo_ecio:evdo_snr)
            CellInfoCdma i = (CellInfoCdma)ce;
            if(i != null){
                String s = "2(";
                CellIdentityCdma g = i.getCellIdentity();
                if(g != null){
                   s += String.format("%d:%d:%d:%d:%d",
                           g.getNetworkId()==Integer.MAX_VALUE?-1:g.getNetworkId(),
                           g.getBasestationId()==Integer.MAX_VALUE?-1:g.getBasestationId(),
                           g.getSystemId()==Integer.MAX_VALUE?-1:g.getSystemId(),
                           g.getLatitude()==Integer.MAX_VALUE?-1:g.getLatitude(),
                           g.getLongitude()==Integer.MAX_VALUE?-1:g.getLongitude());
                }
                s+=")(";
                CellSignalStrengthCdma n = i.getCellSignalStrength();
                if(n != null){
                    s += String.format("%d:%d:%d:%d:%d",
                            n.getCdmaDbm(), n.getCdmaEcio(),
                            n.getEvdoDbm(), n.getEvdoEcio(), n.getEvdoSnr());
                }
                s+=")";
                return s;
            }
        }else if(ce instanceof CellInfoLte){
            //3(tac:ci:pci:mcc:mnc:efcn)(ss:ta:cqi:rsrp:rsrq:rssnr)
            String s = "3(";
            CellInfoLte i = (CellInfoLte)ce;
            if(i != null){
                CellIdentityLte g = i.getCellIdentity();
                if(g != null){
                    s += String.format("%d:%d:%d:%d:%d",
                            g.getTac()==Integer.MAX_VALUE?-1:g.getTac(),
                            g.getCi()==Integer.MAX_VALUE?-1:g.getCi(),
                            g.getPci()==Integer.MAX_VALUE?-1:g.getPci(),
                            g.getMcc()==Integer.MAX_VALUE?-1:g.getMcc(),
                            g.getMnc()==Integer.MAX_VALUE?-1:g.getMnc());
                    if(Build.VERSION.SDK_INT > Build.VERSION_CODES.N){
                        s+=String.format(":%d", g.getEarfcn()==Integer.MAX_VALUE?-1:g.getEarfcn());
                    }
                }
            }
            s+=")(";
            CellSignalStrengthLte n = i.getCellSignalStrength();
            if(n != null){
                /*
                "CellSignalStrengthLte:"
                + " ss=" + mSignalStrength
                + " rsrp=" + mRsrp
                + " rsrq=" + mRsrq
                + " rssnr=" + mRssnr
                + " cqi=" + mCqi
                + " ta=" + mTimingAdvance;
                 */
                String[] arrs = n.toString().split(" ");
                if(arrs != null) {
                    try {
                        int ss = -1;
                        int ta = -1;
                        int rsrp = -1;
                        int rsrq = -1;
                        int rssnr = -1;
                        int cqi = -1;

                        for (String a : arrs) {
                            if (a.startsWith("ss=")) {
                                ss = Integer.parseInt(a.substring(3));
                            } else if (a.startsWith("ta=")) {
                                ta = Integer.parseInt(a.substring(3));
                            } else if (a.startsWith("cqi=")) {
                                cqi = Integer.parseInt(a.substring(4));
                            } else if (a.startsWith("rsrp=")) {
                                rsrp = Integer.parseInt(a.substring(5));
                            } else if (a.startsWith("rsrq=")) {
                                rsrq = Integer.parseInt(a.substring(5));
                            } else if (a.startsWith("rssnr=")) {
                                rssnr = Integer.parseInt(a.substring(6));
                            }
                        }
                        if(ss == Integer.MAX_VALUE) ss = -1;
                        if(ta == Integer.MAX_VALUE) ta = -1;
                        if(cqi == Integer.MAX_VALUE) cqi = -1;
                        if(rsrp == Integer.MAX_VALUE) rsrp = -1;
                        if(rsrq == Integer.MAX_VALUE) rsrq = -1;
                        if(rssnr == Integer.MAX_VALUE) rssnr = -1;
                        s += String.format("%d:%d:%d:%d:%d:%d", ss, ta, cqi, rsrp, rsrq, rssnr);
                    }catch (Exception e2){}
                }
            }
            s+=")";
            return s;
        }else if(ce instanceof CellInfoWcdma){
            //4(lac:cid:psc:mcc:mnc:uarfcn)(ss:ber)
            String s = "4(";
            CellInfoWcdma i = (CellInfoWcdma)ce;
            if(i != null){
                CellIdentityWcdma g = i.getCellIdentity();
                if(g != null){
                    s += String.format("%d:%d:%d:%d:%d",
                            g.getLac()==Integer.MAX_VALUE?-1:g.getLac(),
                            g.getCid()==Integer.MAX_VALUE?-1:g.getCid(),
                            g.getPsc()==Integer.MAX_VALUE?-1:g.getPsc(),
                            g.getMcc()==Integer.MAX_VALUE?-1:g.getMcc(),
                            g.getMnc()==Integer.MAX_VALUE?-1:g.getMnc());
                    if(Build.VERSION.SDK_INT > Build.VERSION_CODES.N){
                        s+=String.format(":%d", g.getUarfcn()==Integer.MAX_VALUE?-1:g.getUarfcn());
                    }
                }
            }
            s+=")(";
            CellSignalStrengthWcdma n = i.getCellSignalStrength();
            if(n != null){
                /*
                return "CellSignalStrengthWcdma:"
                + " ss=" + mSignalStrength
                + " ber=" + mBitErrorRate;
                 */
                String[] arrs = n.toString().split(" ");
                if(arrs != null) {
                    try {
                        int ss = -1;
                        int ber = -1;

                        for (String a : arrs) {
                            if (a.startsWith("ss=")) {
                                ss = Integer.parseInt(a.substring(3));
                            } else if (a.startsWith("ber=")) {
                                ber = Integer.parseInt(a.substring(4));
                            }
                        }
                        if(ss == Integer.MAX_VALUE) ss = -1;
                        if(ber == Integer.MAX_VALUE) ber = -1;
                        s += String.format("%d:%d", ss, ber);
                    }catch (Exception e2){}
                }
            }
            s+=")";

            return s;
        }
        return null;
    }

    private void initCellListener() {
        try {
            TelephonyManager mgr = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
            mgr.listen(phoneStateListener, PhoneStateListener.LISTEN_CELL_LOCATION); //注册监听器，设定不同的监听类型//设置监听器方法
        }catch (Exception e){
            Log.d(TAG, "initCellListener exception "+e+","+Log.getStackTraceString(e));
        }

    }
}
