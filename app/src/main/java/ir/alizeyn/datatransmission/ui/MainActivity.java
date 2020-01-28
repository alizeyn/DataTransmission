package ir.alizeyn.datatransmission.ui;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;

import butterknife.BindView;
import butterknife.ButterKnife;
import ir.alizeyn.datatransmission.Opration;
import ir.alizeyn.datatransmission.R;
import ir.alizeyn.datatransmission.Util;
import ir.alizeyn.datatransmission.model.Coding;
import ir.alizeyn.datatransmission.model.DeModulation;
import ir.alizeyn.datatransmission.model.ErrorDetection;
import ir.alizeyn.datatransmission.model.Process;

public class MainActivity extends AppCompatActivity {

    private static final int ACTIVITY_CHOOSE_FILE = 1002;
    private static final String TAG = MainActivity.class.getName();

    private String src;

    private Process process = Process.ENCODE;
    private Coding coding = Coding.HDB3;
    private ErrorDetection errorDetection = ErrorDetection.CRC;
    private DeModulation deModulation = DeModulation.ASK;

    @BindView(R.id.llFileDetails)
    LinearLayout llFileDetails;
    @BindView(R.id.tvFileName)
    TextView tvFileName;
    @BindView(R.id.tvLog)
    TextView tvLog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
    }

    public void selectFile(View view) {

        if (Util.getStoragePermission(this)) {
            chooseFile();
        }
    }

    private void chooseFile() {

        Intent chooseFile;
        Intent intent;
        chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
        chooseFile.addCategory(Intent.CATEGORY_OPENABLE);
        chooseFile.setType("*/*");
        intent = Intent.createChooser(chooseFile, "Choose a file");
        startActivityForResult(intent, ACTIVITY_CHOOSE_FILE);
    }


    public void startProcess(View view) {

        tvLog.setText("");
        String encodingData = src;

        if (encodingData == null) {
            Toast.makeText(this, "Select input file frist!", Toast.LENGTH_SHORT).show();
            return;
        }

        String initLog = "Input -> " + encodingData + "\n\n";
        tvLog.append(initLog);

        if (process == Process.ENCODE) {

            if (coding == Coding.HDB3) {
                encodingData = Opration.enocdeByHDB3(encodingData);
            } else {
                encodingData = Opration.encodeByB8ZS(encodingData);
            }
            String encodeLog = "Encoded -> " + encodingData + "\n\n";
            tvLog.append(encodeLog);

            if (errorDetection == ErrorDetection.CRC) {
                encodingData = Opration.getCRC32Value(encodingData);
            } else {
                encodingData = Opration.encodeHamming(encodingData);
            }
            String decodeLog = "Code Word -> " + encodingData + "\n\n";
            tvLog.append(decodeLog);

            if (deModulation == DeModulation.ASK) {
                encodingData = Opration.encodeASK(encodingData);
            } else {
                encodingData = Opration.encodeFSK(encodingData);
            }
            String demodulationLog = "Demodulation Encode -> " + encodingData + "\n\n";
            tvLog.append(demodulationLog);

        } else {

            if (coding == Coding.HDB3) {
                encodingData = Opration.decodeFromHDB3(encodingData);
            } else {
                encodingData = Opration.decodeFromB8ZS(encodingData);
            }
            String decodeLog = "Decoded -> " + encodingData + "\n\n";
            tvLog.append(decodeLog);

            if (errorDetection == ErrorDetection.CRC) {
                String errDetectionMsg;
                if (Opration.verifyCRC32Value(encodingData)) {
                    errDetectionMsg = "startProcess: CRC NO ERROR\n\n";
                } else {
                    errDetectionMsg = "startProcess: CRC *ERROR*\n\n";
                }
                tvLog.append(errDetectionMsg);

                encodingData = Opration.removeCRC32Value(encodingData);
            } else {
                encodingData = Opration.correctHammingOrginalData(encodingData);
            }
            String crcLog = "Data Word -> " + encodingData + "\n\n";
            tvLog.append(crcLog);

            if (deModulation == DeModulation.ASK) {
                encodingData = Opration.decodeASK(encodingData);
            } else {
                encodingData = Opration.decodeFSK(encodingData);
            }
            String demoduleDecodeLog = "Demodulation Decode -> " + encodingData + "\n\n";
            tvLog.append(demoduleDecodeLog);
        }

    }

    //<editor-fold desc="Select Methods">
    public void selectDemodulationMethodASK(View view) {
        deModulation = DeModulation.FSK;
    }

    public void selectDemodulationMethodFSK(View view) {
        deModulation = DeModulation.FSK;
    }

    public void selectErrorDetectionMethodCRC(View view) {
            errorDetection = ErrorDetection.CRC;
    }

    public void selectErrorDetectionMethodHamming(View view) {
            errorDetection = ErrorDetection.HAMMING;
    }

    public void selectCodingMethodHDB3(View view) {

            coding = Coding.HDB3;
    }

    public void selectCodingMethodB8ZS(View view) {

            coding = Coding.B8ZS;
    }

    public void selectProcessEncode(View view) {
            process = Process.ENCODE;
    }

    public void selectProcessDecode(View view) {
            process = Process.DECODE;
    }
    //</editor-fold>

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == Util.STORAGE_PERM_REQ_CODE &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                grantResults[1] == PackageManager.PERMISSION_GRANTED) {

            chooseFile();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == ACTIVITY_CHOOSE_FILE) {

            if (data != null) {
                Uri fileUri = data.getData();

                if (fileUri != null) {

                    Toast.makeText(this, fileUri.toString(), Toast.LENGTH_SHORT).show();
                    tvFileName.setText(fileUri.getLastPathSegment());
                    llFileDetails.setVisibility(View.VISIBLE);

                    try {
                        InputStream inputStream = getContentResolver().openInputStream(fileUri);
                        if (inputStream != null) {
                            String fileContent = IOUtils.toString(inputStream, "UTF-8");
                            for (Character c :
                                    fileContent.toCharArray()) {
                                fileContent += String.format("%8s", Integer.toBinaryString(c)).replace(' ', '0');
                            }
                            src = fileContent;
                            Log.i(TAG, "onActivityResult: " + fileContent);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
