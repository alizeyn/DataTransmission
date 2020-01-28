package ir.alizeyn.datatransmission;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    private static final int ACTIVITY_CHOOSE_FILE = 1002;

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
                }
            }
        }
    }
}
