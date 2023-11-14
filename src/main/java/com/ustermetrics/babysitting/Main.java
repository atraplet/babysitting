package com.ustermetrics.babysitting;

import com.google.ortools.Loader;
import com.google.ortools.sat.CpModel;
import com.google.ortools.sat.CpSolver;
import com.google.ortools.sat.IntVar;

import java.util.ArrayList;
import java.util.List;

import static com.google.ortools.sat.CpSolverStatus.FEASIBLE;
import static com.google.ortools.sat.CpSolverStatus.OPTIMAL;
import static com.google.ortools.sat.LinearExpr.weightedSum;

public class Main {

    /**
     * (Dell Logic puzzle, 1998)
     * <p>
     * Each weekday, Bonnie takes care of five of the neighbors' children. The children's names are Keith, Libby,
     * Margo, Nora, and Otto; last names are Fell, Grant, Hall, Ivey, and Jule. Each is a different number of years
     * old, from two to six. Can you find each child's full name and age?
     * <p>
     * 1. One child is named Libby Jule. 2. Keith is one year older than the Ivey child, who is one year older than
     * Nora. 3. The Fell child is three years older than Margo. 4. Otto is twice as many years old as the Hall child.
     */
    public static void main(String[] args) {
        Loader.loadNativeLibraries();

        var last = List.of("Keith", "Libby", "Margo", "Nora", "Otto");
        var first = List.of("Fell", "Grant", "Hall", "Ivey", "Jule");

        var model = new CpModel();

        var lastAge = new ArrayList<IntVar>();
        var firstAge = new ArrayList<IntVar>();
        for (int i = 0; i < last.size(); i++) {
            lastAge.add(model.newIntVar(2, 6, last.get(i)));
            firstAge.add(model.newIntVar(2, 6, first.get(i)));
        }

        // Each is a different number of years old
        model.addAllDifferent(lastAge);
        model.addAllDifferent(firstAge);

        // One child is named Libby Jule
        model.addEquality(lastAge.get(1), firstAge.get(4));

        // Keith is one year older than the Ivey child, who is one year older than Nora
        model.addEquality(weightedSum(new IntVar[]{lastAge.get(0), firstAge.get(3)}, new long[]{1, -1}), 1);
        model.addEquality(weightedSum(new IntVar[]{firstAge.get(3), lastAge.get(3)}, new long[]{1, -1}), 1);

        // The Fell child is three years older than Margo
        model.addEquality(weightedSum(new IntVar[]{firstAge.get(0), lastAge.get(2)}, new long[]{1, -1}), 3);

        // Otto is twice as many years old as the Hall child
        model.addEquality(weightedSum(new IntVar[]{lastAge.get(4), firstAge.get(2)}, new long[]{1, -2}), 0);

        var solver = new CpSolver();
        var status = solver.solve(model);

        if (status == OPTIMAL || status == FEASIBLE) {
            for (int i = 0; i < last.size(); i++) {
                long age = solver.value(lastAge.get(i));
                for (int j = 0; j < last.size(); j++) {
                    if (age == solver.value(firstAge.get(j))) {
                        System.out.println(last.get(i) + ", " + first.get(j) + ": " + age + "y");
                    }
                }
            }
        } else {
            System.out.println("No solution found");
        }
    }

}
