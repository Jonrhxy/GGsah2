package com.example.firstpage;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class LeaderBoards extends AppCompatActivity {

    private FirebaseFirestore db;
    private TextView firstPlaceName, firstPlaceScore;
    private TextView secondPlaceName, secondPlaceScore;
    private TextView thirdPlaceName, thirdPlaceScore;

    private RecyclerView remainingLeaderboard;
    private RecyclerView.Adapter<RecyclerView.ViewHolder> adapter;
    private List<GameData> remainingPlayers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leader_boards);

        // Initialize Firebase Firestore
        db = FirebaseFirestore.getInstance();

        // Initialize TextViews for top 3
        firstPlaceName = findViewById(R.id.first_place_name);
        firstPlaceScore = findViewById(R.id.first_place_rank);
        secondPlaceName = findViewById(R.id.second_place_name);
        secondPlaceScore = findViewById(R.id.second_place_rank);
        thirdPlaceName = findViewById(R.id.third_place_name);
        thirdPlaceScore = findViewById(R.id.third_place_rank);

        // Initialize RecyclerView for remaining leaderboard
        remainingLeaderboard = findViewById(R.id.remaining_leaderboard);
        remainingLeaderboard.setLayoutManager(new LinearLayoutManager(this));
        remainingPlayers = new ArrayList<>();

        // Load leaderboard data
        loadLeaderBoardData();
    }

    private void loadLeaderBoardData() {
        db.collection("Games")
                .orderBy("points", Query.Direction.DESCENDING) // Sort by points in descending order
                .limit(10) // Limit to the top 10 players
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<GameData> leaderboardData = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            GameData gameData = document.toObject(GameData.class);
                            gameData.username = document.getId(); // Assume the document ID is the username
                            leaderboardData.add(gameData);
                        }

                        // Update the UI with sorted data
                        updateLeaderboardUI(leaderboardData);
                    } else {
                        System.err.println("Error getting documents: " + task.getException());
                    }
                });
    }

    private void updateLeaderboardUI(List<GameData> leaderboardData) {
        // Update Top 1
        if (leaderboardData.size() > 0) {
            GameData firstPlace = leaderboardData.get(0);
            firstPlaceName.setText(firstPlace.username);
            firstPlaceScore.setText("Points: " + firstPlace.points + " | High Score: " + firstPlace.highScore);
        }

        // Update Top 2
        if (leaderboardData.size() > 1) {
            GameData secondPlace = leaderboardData.get(1);
            secondPlaceName.setText(secondPlace.username);
            secondPlaceScore.setText("Points: " + secondPlace.points + " | High Score: " + secondPlace.highScore);
        }

        // Update Top 3
        if (leaderboardData.size() > 2) {
            GameData thirdPlace = leaderboardData.get(2);
            thirdPlaceName.setText(thirdPlace.username);
            thirdPlaceScore.setText("Points: " + thirdPlace.points + " | High Score: " + thirdPlace.highScore);
        }

        // Update RecyclerView for remaining players
        if (leaderboardData.size() > 3) {
            remainingPlayers.clear();
            remainingPlayers.addAll(leaderboardData.subList(3, leaderboardData.size()));
            if (adapter == null) {
                adapter = new RecyclerView.Adapter<RecyclerView.ViewHolder>() {
                    @Override
                    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                        // Inflate a simple item view for each remaining player
                        View view = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_2, parent, false);
                        return new RecyclerView.ViewHolder(view) {};
                    }

                    @Override
                    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
                        GameData player = remainingPlayers.get(position);
                        TextView text1 = holder.itemView.findViewById(android.R.id.text1);
                        TextView text2 = holder.itemView.findViewById(android.R.id.text2);

                        text1.setText((position + 4) + ". " + player.username); // Rank + username
                        text2.setText("Points: " + player.points); // Points
                    }

                    @Override
                    public int getItemCount() {
                        return remainingPlayers.size();
                    }
                };
                remainingLeaderboard.setAdapter(adapter);
            } else {
                adapter.notifyDataSetChanged();
            }
        }
    }

    // Game data model class for Firestore
    public static class GameData {
        public String username; // To store the player's username
        public int points;
        public int highScore;

        public GameData() {
            // Default constructor required for Firestore
        }

        public GameData(String username, int points, int highScore) {
            this.username = username;
            this.points = points;
            this.highScore = highScore;
        }
    }
}
