package ir.alizeyn.datatransmission;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
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

    }

    //<editor-fold desc="Select Methods">
    public void selectDemodulationMethod(View view) {
        int selectedId = ((RadioGroup)view).getCheckedRadioButtonId();

        if (selectedId == R.id.rbAsk) {
            deModulation = DeModulation.ASK;
        } else {
            deModulation = DeModulation.FSK;
        }
    }

    public void selectErrorDetectionMethod(View view) {
        int selectedId = ((RadioGroup)view).getCheckedRadioButtonId();

        if (selectedId == R.id.rbCrc) {
            errorDetection = ErrorDetection.CRC;
        } else {
            errorDetection = ErrorDetection.HAMMING;
        }
    }

    public void selectCodingMethod(View view) {
        int selectedId = ((RadioGroup)view).getCheckedRadioButtonId();

        if (selectedId == R.id.rbHdb3) {
            coding = Coding.HDB3;
        } else {
            coding = Coding.B8ZS;
        }
    }

    public void selectProcess(View view) {
        int selectedId = ((RadioGroup)view).getCheckedRadioButtonId();

        if (selectedId == R.id.rbEncode) {
            process = Process.ENCODE;
        } else {
            process = Process.DECODE;
        }
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
