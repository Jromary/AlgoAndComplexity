import com.sun.corba.se.impl.orbutil.graph.Graph;

import java.awt.image.AreaAveragingScaleFilter;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
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
        public int size;
        public ArrayList<Integer> conflit;
        public Sommet(int id, int couleur, ArrayList<Integer> conflit, int size) {
            this.id = id;
            this.couleur = couleur;
            this.conflit = conflit;
            this.size = size;
        }

        public Sommet(int id, int size){
            this(id, -1, new ArrayList<>(), size);
        }

        public Sommet(int id){
            this(id, 0);
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
//        System.out.println("nombre de boite : "+ rengement.size());
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
                    //System.out.println(s);
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




    public static ArrayList<Boite>  DsaturWithFFDpacking(ArrayList<Sommet> sommets, int size_boite){
        ArrayList<Tuple> tuples = new ArrayList<>();

        for (int i= 0 ; i<sommets.size() ; i++){
            ArrayList<Integer> conflit = new ArrayList<>();
            for (int j= 0 ; j< sommets.size() ; j++){
                if (i == j)
                    continue;
                if (sommets.get(i).couleur != sommets.get(j).couleur){
                    conflit.add(sommets.get(j).id);
                }
            }
            tuples.add(new Tuple(sommets.get(i).id , sommets.get(i).size ,  conflit));
            //System.out.println("sommet :" + sommets.get(i).id + "   couleur :"+sommets.get(i).couleur+ "    size = "+h );

        }

        Collections.sort(tuples);

        ArrayList<Boite> rangement = fitDecreasingPacking(tuples, size_boite);
        return rangement;


    }



    public static ArrayList<Boite>  DsaturWithBFDpacking(ArrayList<Sommet> sommets, int size_boite){
        ArrayList<Tuple> tuples = new ArrayList<>();

        for (int i= 0 ; i<sommets.size() ; i++){
            ArrayList<Integer> conflit = new ArrayList<>();
            for (int j= 0 ; j< sommets.size() ; j++){
                if (i == j)
                    continue;
                if (sommets.get(i).couleur != sommets.get(j).couleur){
                    conflit.add(sommets.get(j).id);
                }
            }
            tuples.add(new Tuple(sommets.get(i).id , sommets.get(i).size ,  conflit));
            //System.out.println("sommet :" + sommets.get(i).id + "   couleur :"+sommets.get(i).couleur+ "    size = "+h );

        }

        Collections.sort(tuples);

        ArrayList<Boite> rangement = bestFitDecreasingPacking(tuples, size_boite);
        return rangement;
    }



    public static void main(String[] args) throws FileNotFoundException {

        Scanner sc = new Scanner(new BufferedReader(new FileReader("DSJC250.5.txt")));

        ArrayList<Sommet> g = new ArrayList<>();
        ArrayList<Tuple> o = new ArrayList<>();

        while(sc.hasNextLine()) {
                String[] line = sc.nextLine().trim().split(" ");

            if (line[0].equals("p")){
                //System.out.println("yes  "+Integer.parseInt(line[2]));
                for (int i=1 ; i <= Integer.parseInt(line[2]) ; i++){
                    int h = 10 + (int) (Math.random() * (50));
                    g.add(new Sommet(i, h));

                    o.add(new Tuple(i , h));
                }
                continue;
            }

            if (!line[0].equals("e"))
                    continue;

               if(line[0].equals("e")) {

                   g.get(Integer.parseInt(line[1]) - 1).addConflict(g.get(Integer.parseInt(line[2]) - 1).id);
                   g.get(Integer.parseInt(line[2]) - 1).addConflict(g.get(Integer.parseInt(line[1]) - 1).id);
                   o.get(Integer.parseInt(line[2]) - 1).conflit.add(g.get(Integer.parseInt(line[1]) - 1).id);
                   o.get(Integer.parseInt(line[2]) - 1).conflit.add(g.get(Integer.parseInt(line[1]) - 1).id);
               }

        }



        /*for (int k = 0 ; k< g.size() ; k++){
            int l = k+1;
            System.out.print("le sommet " + l + " est en conflit avec  ");
            for (int j=0 ; j< g.get(k).conflit.size() ; j++){
                System.out.print(g.get(k).conflit.get(j)+"  ");

            }
            System.out.println();
        }*/

//        ArrayList<Integer> conflit = new ArrayList<>();
//        conflit.add(2);
//        Tuple obj1 = new Tuple(1, 2, conflit);
//        Tuple obj2 = new Tuple(2, 4);
//        Tuple obj3 = new Tuple(3, 3);
//        Tuple obj4 = new Tuple(4, 1);
//        ArrayList<Tuple> objets = new ArrayList<>();
//        objets.add(obj1);
//        objets.add(obj2);
//        objets.add(obj3);
//        objets.add(obj4);

        int min = Integer.MAX_VALUE;
        int max = 0;
        int somme = 0;



        ///test 1 (q1)
        System.out.println("FractionalPacking :\n");
        System.out.println(fractionalPackaging(o, 150));
        System.out.println("\n*********************************************\n");

        ///test 2 (q2)
        System.out.println("FirstFitDecreasingPacking :\n");
        Collections.sort(o);

        min = Integer.MAX_VALUE;
        max = 0;
        somme = 0;
        for (int i = 0; i < 1000; i++) {
            int value = fitDecreasingPacking(o , 150).size();
            if (value < min){
                min = value;
            }
            if (value > max) {
                max = value;
            }
            somme = somme + value;
        }
        System.out.println("["+ min + "|" + max + "|" + ((float)somme/1000.0) +"]");


        //System.out.println(fitDecreasingPacking(o, 150).size());
        System.out.println("\n*********************************************\n");


        ///q3
        System.out.println("BestFitDecreasingPacking :\n");
        Collections.sort(o);
        min = Integer.MAX_VALUE;
        max = 0;
        somme = 0;
        for (int i = 0; i < 1000; i++) {
            int value = bestFitDecreasingPacking(o , 150).size();
            if (value < min){
                min = value;
            }
            if (value > max) {
                max = value;
            }
            somme = somme + value;
        }
        System.out.println("["+ min + "|" + max + "|" + ((float)somme/1000.0) +"]");

        System.out.println("\n*********************************************\n");


        ///q4
//        Sommet sommet1 = new Sommet(1);
//        sommet1.addConflict(2);
//        sommet1.addConflict(3);
//        sommet1.addConflict(4);
//        sommet1.addConflict(5);
//        Sommet sommet2 = new Sommet(2);
//        sommet2.addConflict(1);
//        sommet2.addConflict(3);
//        sommet2.addConflict(4);
//        sommet2.addConflict(5);
//        Sommet sommet3 = new Sommet(3);
//        Sommet sommet4 = new Sommet(4);
//        Sommet sommet5 = new Sommet(5);
//
//        ArrayList<Sommet> graph = new ArrayList<>();
//        graph.add(sommet1);
//        graph.add(sommet2);
//        graph.add(sommet3);
//        graph.add(sommet4);
//        graph.add(sommet5);


        //q4
        ArrayList<Sommet> sommets = dsatur(g);


        //q5
        System.out.println("DsaturWithFFDpacking :\n");
         min = Integer.MAX_VALUE;
         max = 0;
         somme = 0;
        for (int i = 0; i < 1000; i++) {
            int value = DsaturWithFFDpacking(sommets , 150).size();
            if (value < min){
                min = value;
            }
            if (value > max) {
                max = value;
            }
            somme = somme + value;
        }
        System.out.println("["+ min + "|" + max + "|" + ((float)somme/1000.0) +"]");
//        System.out.println(DsaturWithFFDpacking(sommets , 150).size());
        System.out.println("\n*********************************************\n");


        //q6
        System.out.println("DsaturWithBFDpacking :\n");


        min = Integer.MAX_VALUE;
        max = 0;
        somme = 0;
        for (int i = 0; i < 1000; i++) {
            int value = DsaturWithBFDpacking(sommets , 150).size();
            if (value < min){
                min = value;
            }
            if (value > max) {
                max = value;
            }
            somme = somme + value;
        }
        System.out.println("["+ min + "|" + max + "|" + ((float)somme/1000.0) +"]");



        //System.out.println(DsaturWithBFDpacking(sommets , 150).size());
        System.out.println("\n*********************************************\n");


    }
}
