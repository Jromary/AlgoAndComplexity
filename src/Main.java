import java.lang.reflect.Array;
import java.util.*;

public class Main {
    //class pour les objets
    public static class Tuple implements Comparable<Tuple>{
        public int id;
        public int size;
        public ArrayList<Integer> conflit;

        public Tuple(int id, int size, ArrayList<Integer> conflit) {
            this.id = id;
            this.size = size;
            this.conflit = conflit;
        }

        public Tuple(int id, int size){
            this(id, size, new ArrayList<>());
        }

        @Override
        public String toString() {
            return "[" + id + ", " + size + "]";
        }

        @Override
        public int compareTo(Tuple tuple) {
            return (Integer.compare(tuple.size, this.size));
        }
    }
    //class pour les boites
    public static class Boite{
        public int taille;
        public ArrayList<Tuple> occupation;
        public Boite(int taille){
            this.taille = taille;
            this.occupation = new ArrayList<>();
        }

        public void add(Tuple objet){
            occupation.add(objet);
        }

        public int sizeLeft(){
            int size = 0;
            for (Tuple t : occupation) {
                size = size + t.size;
            }
            return taille - size;
        }

        public boolean conflict(Tuple objet){
            for (Tuple o : occupation) {
                if(o.conflit.contains(objet.id) || objet.conflit.contains(o.id)){
                    return true;
                }
            }
            return false;
        }

        @Override
        public String toString() {
            return "Boite{" +
                    "taille=" + taille + ", size_left=" + sizeLeft() +
                    ", occupation=" + occupation +
                    '}';
        }
    }
    //class pour les sommet du graph
    public static class Sommet implements Comparable<Sommet>{
        public int id;
        public int couleur;
        public ArrayList<Integer> conflit;
        public Sommet(int id, int couleur, ArrayList<Integer> conflit) {
            this.id = id;
            this.couleur = couleur;
            this.conflit = conflit;
        }

        public Sommet(int id){
            this(id, -1, new ArrayList<>());
        }

        public void addConflict(int id){
            conflit.add(id);
        }

        @Override
        public int compareTo(Sommet sommet) {
            return (Integer.compare(sommet.conflit.size(), this.conflit.size()));
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Sommet sommet = (Sommet) o;
            return id == sommet.id;
        }

        @Override
        public int hashCode() {
            return Objects.hash(id);
        }

        @Override
        public String toString() {
            return "Sommet{" + id + " : " + couleur +
                    '}';
        }
    }

    public static int fractionalPackaging(List<Tuple> objets, int size_boite){
        int size_total = 0;
        for (Tuple t: objets){
            size_total = size_total + t.size;
        }
        return (int) Math.ceil((double)size_total / size_boite);
    }

    public static ArrayList<Boite> fitDecreasingPacking(List<Tuple> objets, int size_boite){
        ArrayList<Boite> rengement = new ArrayList<>();
        for (Tuple objet: objets) {
            boolean insert = false;
            for (Boite boite: rengement){
                if(!insert){
                    if(!boite.conflict(objet)){
                        if (objet.size <= boite.sizeLeft()){
                            boite.add(objet);
                            insert = true;
                        }
                    }
                }
            }
            if(!insert){
                Boite boite = new Boite(size_boite);
                if (objet.size <= boite.sizeLeft()){
                    boite.add(objet);
                    insert = true;
                }else {
                    throw new IllegalArgumentException("Objet plus gros que la boite");
                }
                rengement.add(boite);
            }
        }
        return rengement;
    }

    public static ArrayList<Boite> bestFitDecreasingPacking(List<Tuple> objets, int size_boite){
        ArrayList<Boite> rengement = new ArrayList<>();
        for (Tuple objet: objets) {
            boolean insert = false;
            int idToInsert = 0;
            int sizeLeftBest = -1;
            for (Boite boite: rengement){
                if(!boite.conflict(objet)){
                    if (objet.size <= boite.sizeLeft()){
                        if (boite.sizeLeft() - objet.size > sizeLeftBest){
                            insert = true;
                            idToInsert = rengement.indexOf(boite);
                            sizeLeftBest = boite.sizeLeft() - objet.size;
                        }
                    }
                }
            }
            if(!insert){
                Boite boite = new Boite(size_boite);
                if (objet.size <= boite.sizeLeft()){
                    boite.add(objet);
                    insert = true;
                }else {
                    throw new IllegalArgumentException("Objet plus gros que la boite");
                }
                rengement.add(boite);
            }else {
                rengement.get(idToInsert).add(objet);
            }
        }
        return rengement;
    }

    public static ArrayList<Sommet> dsatur(ArrayList<Sommet> U){
        //Initialisation : C := ∅ and U := V. Ordonner les sommets par ordre décroissant de degrés.
        ArrayList<Sommet> C = new ArrayList<>();
        ArrayList<Sommet> V = new ArrayList<>(U);
        Collections.sort(U);
        Collections.sort(V);

        //identifier le sommet v ∈U de degré maximal et affecter la couler 1 au sommet v. C := C ∪ {v} et U:= U\{v}.
        Sommet premier = U.get(0);
        premier.couleur = 1;
        U.remove(premier);
        C.add(premier);
        while (!C.equals(V)){
            int maxSaturation = -1;
            int maxDeg = -1;
            int sommetId = 0;
            //pour chaque sommet
            for (Sommet sommet: U) {
                int saturation = 0;
                for (int i : sommet.conflit) {
                    if (C.contains(new Sommet(i))){
                        saturation++;
                    }
                }
                if (saturation >= maxSaturation){
                    if (sommet.conflit.size() > maxDeg){
                        maxDeg = sommet.conflit.size();
                        maxSaturation = saturation;
                        sommetId = U.indexOf(sommet);
                    }
                }
            }

            //Attribuer à v le numéro de couleur le plus petit possible. C:= C ∪ {v} and U := U\{v}
            int couleur = 0;
            boolean trouve = false;
            Sommet v = U.get(sommetId);
            while(!trouve){
                trouve = true;
                couleur++;
                for (int s : v.conflit) {
                    System.out.println(s);
                    Sommet tempo = new Sommet(s);
                    if (C.contains(tempo)){
                        if (C.get(C.indexOf(new Sommet(s))).couleur == couleur){
                            trouve = false;
                            break;
                        }
                    }
                }
            }
            v.couleur = couleur;
            C.add(v);
            U.remove(v);
        }
        return C;
    }

    public static void main(String[] args) {
        ArrayList<Integer> conflit = new ArrayList<>();
        conflit.add(2);
        Tuple obj1 = new Tuple(1, 2, conflit);
        Tuple obj2 = new Tuple(2, 4);
        Tuple obj3 = new Tuple(3, 3);
        Tuple obj4 = new Tuple(4, 1);
        ArrayList<Tuple> objets = new ArrayList<>();
        objets.add(obj1);
        objets.add(obj2);
        objets.add(obj3);
        objets.add(obj4);

        ///test 1 (q1)
        System.out.println(fractionalPackaging(objets, 10));

        ///test 2 (q2)
        Collections.sort(objets);
        System.out.println(fitDecreasingPacking(objets, 10));

        ///q3
        Collections.sort(objets);
        System.out.println(bestFitDecreasingPacking(objets, 10));

        ///q4
        Sommet sommet1 = new Sommet(1);
        sommet1.addConflict(2);
        sommet1.addConflict(3);
        sommet1.addConflict(4);
        sommet1.addConflict(5);
        Sommet sommet2 = new Sommet(2);
        sommet2.addConflict(1);
        sommet2.addConflict(3);
        sommet2.addConflict(4);
        sommet2.addConflict(5);
        Sommet sommet3 = new Sommet(3);
        Sommet sommet4 = new Sommet(4);
        Sommet sommet5 = new Sommet(5);

        ArrayList<Sommet> graph = new ArrayList<>();
        graph.add(sommet1);
        graph.add(sommet2);
        graph.add(sommet3);
        graph.add(sommet4);
        graph.add(sommet5);

        System.out.println(dsatur(graph));

    }
}
