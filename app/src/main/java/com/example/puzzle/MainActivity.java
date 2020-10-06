package com.example.puzzle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import java.util.ArrayList;

import static java.lang.StrictMath.abs;

public class MainActivity extends AppCompatActivity {

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final RelativeLayout layout = findViewById(R.id.layout);


       // final ConstraintLayout layout = findViewById(R.id.layout);
        ImageView imageView = findViewById(R.id.imageView);

        //On transforme l'image en bitmap
        Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.photo);

        //On redéfini le dimensions de l'image en fonction des dimensions  l'ecran
        Bitmap scaled = Bitmap.createScaledBitmap(bm, getWindowManager().getDefaultDisplay().getWidth(), getWindowManager().getDefaultDisplay().getHeight(), true);

        //on Set l'image a l'imageView pour l'afficher a l'ecran
        imageView.setImageBitmap(scaled);



        imageView.post(new Runnable() {
            @Override
            public void run() {
                ArrayList<Bitmap> pieces = splitImage(); //on decoupe l'image
                TouchListener touchListener = new TouchListener();

                for(Bitmap piece : pieces) {
                    ImageView iv = new ImageView(getApplicationContext());

                    iv.setImageBitmap(piece); //on ajoute chaque piece
                    iv.setOnTouchListener(touchListener);
                    layout.addView(iv);
                }
            }
        });

        /* // test des matrices (pour la rotation ect
        //imageView.setMaxWidth(1000);
       // imageView.setMaxHeight(1000);

        //On transforme l'image en bitmap
        Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.photo);


        //On redéfini le dimensions de l'image en fonction des dimensions  l'ecran
        Matrix matrix = new Matrix();
        matrix.setRotate(90);
        Bitmap scaled = Bitmap.createScaledBitmap(bm, getWindowManager().getDefaultDisplay().getWidth(), getWindowManager().getDefaultDisplay().getHeight(), true);
        Bitmap  bOutput = Bitmap.createBitmap(scaled, 0, 0, scaled.getWidth(), scaled.getHeight(), matrix, true);
         //scaled = Bitmap.createScaledBitmap(bm, 50,50, true);

        //on Set l'image a l'imageView pour l'afficher a l'ecran
        imageView.setImageBitmap(bOutput);

        imageView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.photo);

                ImageView imageView = new ImageView(getApplicationContext());

                Matrix matrix = new Matrix();
                imageView.setScaleType(ImageView.ScaleType.MATRIX);   //required
                matrix.setRotate(180);

                Bitmap bmp=Bitmap.createBitmap(bitmap, 0, 0,bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                bitmap.recycle();
                bitmap=bmp;
                imageView.setImageBitmap(bitmap);

            }

                                          });


*/
    }

    private ArrayList<Bitmap> splitImage() {
        int nbpieces = 4;
        int rows = 2;
        int cols = 2;

        ImageView imageView = findViewById(R.id.imageView);
        ArrayList<Bitmap> pieces = new ArrayList<>(nbpieces);

        // convertion de l'image en bitmap
        BitmapDrawable drawable = (BitmapDrawable) imageView.getDrawable();
        Bitmap bitmap = drawable.getBitmap();



        //on crée une nvl image avec les nvl dimentions
        int[] dimensions = getPiecePoisition(imageView);
        int scaledBitmapLeft = dimensions[0];
        int scaledBitmapTop = dimensions[1];
        int scaledBitmapWidth = dimensions[2];
        int scaledBitmapHeight = dimensions[3];

        int imageWidth = scaledBitmapWidth - 2 * abs(scaledBitmapLeft);
        int imageHeight = scaledBitmapHeight - 2 * abs(scaledBitmapTop);

        Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, scaledBitmapWidth, scaledBitmapHeight, true);
        Bitmap croppedBitmap = Bitmap.createBitmap(scaledBitmap, abs(scaledBitmapLeft), abs(scaledBitmapTop), imageWidth, imageHeight);


        // Calcul de la largeur et de la longueur des nvl pieces
        int pieceWidth = imageWidth/cols;
        int pieceHeight = imageHeight/rows;

        // creation de chaque bitmap en fonction de la largeur et de la longueur
        int yCoord = 0;
        for (int i = 0; i < rows; i++) {
            int xCoord = 0;
            for (int j = 0; j < cols; j++) {
                pieces.add(Bitmap.createBitmap(croppedBitmap, xCoord, yCoord, pieceWidth, pieceHeight));
                xCoord += pieceWidth;
            }
            yCoord += pieceHeight;
        }

        return pieces;
    }

    private int[] getPiecePoisition(ImageView imageView) {
        int[] res = new int[4];

        if (imageView == null || imageView.getDrawable() == null)
            return res;

        //Pour obtenir les dimensions réelles de l'image affichée  on passe par la matrice associé a  l image

        float[] tab  = new float[9]; //stocké toute les valeurs de la matrice
        imageView.getImageMatrix().getValues(tab); // on stock les values dans un tableau
        Log.e("test",  imageView.getImageMatrix().toString());
        Log.e("test", tab.toString());

        // on stock les valeurs x et y de la matrice
        final float scaleX = tab[Matrix.MSCALE_X];
        final float scaleY = tab[Matrix.MSCALE_Y];

        Log.e("test", String.valueOf(scaleX));
        Log.e("test",String.valueOf(scaleY));

        // Get la  largeur/hauteur  de l'image
        final Drawable d = imageView.getDrawable();
        final int width = d.getIntrinsicWidth();
        final int height = d.getIntrinsicHeight();

        //On calcule les nouvelles dimentions
        final int newWidth = Math.round(width * scaleX);
        final int newHeight = Math.round(height * scaleY);

        res[2] = newWidth;
        res[3] = newHeight;

        // On met la nouvelle image au milieu
        int imWidth = imageView.getWidth();
        int imHeight= imageView.getHeight();

        int top = (int) (imHeight - newHeight)/2;
        int left = (int) (imWidth - newWidth)/2;

        res[0] = left;
        res[1] = top;
        return res;
    }

    public class TouchListener implements View.OnTouchListener {
        private float xDelta;
        private float yDelta;


        @Override
        public boolean onTouch(View v, MotionEvent event) {
            final RelativeLayout layout = findViewById(R.id.layout);

            float x = event.getRawX();
            float y = event.getRawY();
            RelativeLayout.LayoutParams lParams = (RelativeLayout.LayoutParams) v.getLayoutParams();
          //  Log.e("l", String.valueOf(layout.getWidth()));


            switch (event.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_DOWN:
                    xDelta = x - lParams.leftMargin;
                    yDelta = y - lParams.topMargin;
                    break;
                case MotionEvent.ACTION_MOVE:
                    if ((int) (x - xDelta) <=  (layout.getWidth()- v.getWidth())  && (int) (x - xDelta)> 0) {
                        lParams.leftMargin = (int) (x - xDelta);
                    }
                    Log.e("l2", String.valueOf(v.getWidth()));
                    if ((int) (y - yDelta) <=  (layout.getHeight() - v.getHeight())   && (int) (y - yDelta)> 0) {
                        lParams.topMargin = (int) (y - yDelta);
                    }
                    v.setLayoutParams(lParams);
                    break;
            }

            return true;

        }
    }

}
