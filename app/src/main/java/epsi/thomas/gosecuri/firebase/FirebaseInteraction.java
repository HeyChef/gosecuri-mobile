package epsi.thomas.gosecuri.firebase;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import epsi.thomas.gosecuri.entity.Personne;

public class FirebaseInteraction {

    private static FirebaseInteraction INSTANCE = new FirebaseInteraction();
    private DatabaseReference mDatabase;

    private FirebaseInteraction() {
        mDatabase = FirebaseDatabase.getInstance().getReference();
    }

    public static final FirebaseInteraction getInstance() {
        return INSTANCE;
    }

    public void addUser(Personne personne) {
        mDatabase.child("personnes").child(personne.getId()).setValue(personne);
    }
}
