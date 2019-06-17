package epsi.thomas.gosecuri.firebase;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

import epsi.thomas.gosecuri.entity.Personne;
import epsi.thomas.gosecuri.service.IndexService;

public class FirebaseInteraction {

    private static FirebaseInteraction INSTANCE = new FirebaseInteraction();
    private DatabaseReference mDatabase;

    private FirebaseInteraction() {
        mDatabase = FirebaseDatabase.getInstance().getReference("personnes");
    }

    public static final FirebaseInteraction getInstance() {
        return INSTANCE;
    }

    public void addUser(Personne personne) {
        mDatabase.child(personne.getId()).setValue(personne);
    }

    public void test(String s, String a) {
        mDatabase.child(a).setValue(s);
    }


    public void updateUser(Personne personne) {
        Map<String, Object> postValues = personne.toMap();

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put(personne.getId(), postValues);

        mDatabase.updateChildren(childUpdates);
    }

    public void checkUser(final Personne personne, final IndexService indexService) {
        mDatabase.child(personne.getId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Boolean isValid = false;
                if(dataSnapshot.getValue() != null){
                    if(dataSnapshot.child("prenom").getValue().toString().equals(personne.getPrenom()) && dataSnapshot.child("nom").getValue().toString().equals(personne.getNom())) {
                        isValid = true;
                        personne.setImageURL(dataSnapshot.child("imageURL").getValue().toString());
                    }
                }
                indexService.checkResult(isValid, personne);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }
}
