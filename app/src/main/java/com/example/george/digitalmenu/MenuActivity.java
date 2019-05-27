package com.example.george.digitalmenu;

import android.content.Intent;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

public class MenuActivity extends AppCompatActivity {

    private static final String TAG = "MenuActivity";
    private RestaurantDatabase db;

    private ConstraintLayout rootLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu2);
        rootLayout = findViewById(R.id.rootLayout);

        // Get the Intent that started this activity and extract the string
        Intent intent = getIntent();
        String restaurantName = intent.getStringExtra(MainActivity.INTENT_KEY);

        createMenu(restaurantName);
    }

    private void createMenu(String restaurantName) {
        final FirebaseAuth mAuth = FirebaseAuth.getInstance();
        mAuth.signInAnonymously()
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInAnonymously:success");
                        FirebaseUser user = mAuth.getCurrentUser();

                        db = new RestaurantFirestore();
                        db.getRestaurant(restaurantName, r -> displayMenu(r));
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "signInAnonymously:failure", task.getException());
                        Toast.makeText(getApplicationContext(), "Authentication failed.",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void displayMenu(Restaurant r) {
        displayThemePicture(r);
        displayCategories(r);
    }

    private void displayCategories(Restaurant r) {
        List<String> categories = r.getCategories();
        for (String c: categories) {
            displayCategory(r, c);
        }
    }

    private void displayCategory(Restaurant r, String c) {
        LayoutInflater inflater = getLayoutInflater();
        LinearLayout menuPanel = rootLayout.findViewById(R.id.menuPanel);
        LinearLayout clist = (LinearLayout) inflater.inflate(R.layout.category_list, menuPanel, false);

        TextView infoText = clist.findViewById(R.id.categoryText);
        infoText.setText(c);

        displayDishes(r.getDishes(c), clist);
        clist.setId(View.generateViewId());
        menuPanel.addView(clist);
    }

    private void displayDishes(List<Dish> dishes, LinearLayout clist) {
        for (Dish d: dishes) {
            displayDish(d, clist);
        }
    }

    private void displayThemePicture(Restaurant r) {
        ImageView picture = rootLayout.findViewById(R.id.themePicture);
        db.downloadThemePicture(r, bm -> picture.setImageBitmap(bm));
    }

    private void displayDish(Dish d, LinearLayout clist) {

        /* Create card view with fields. */
        LayoutInflater inflater = getLayoutInflater();
        ConstraintLayout dishCard = (ConstraintLayout) inflater.inflate(R.layout.dish_card, clist, false);

        TextView infoText = dishCard.findViewById(R.id.description);
        infoText.setText(d.getDescription());

        TextView nameText = dishCard.findViewById(R.id.name);
        nameText.setText(d.getName());

        TextView priceText = dishCard.findViewById(R.id.price);
        priceText.setText(String.valueOf(d.getPrice()));

        ImageView foodImage = dishCard.findViewById(R.id.foodPicture);
        db.downloadDishPicture(d, bm -> foodImage.setImageBitmap(bm));

        dishCard.setId(View.generateViewId());

        // Add to existing list of cards
        clist.addView(dishCard);
    }
}
