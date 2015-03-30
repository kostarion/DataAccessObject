package classes;

import annotations.KeyField;
import annotations.TaggedObject;

@TaggedObject(name = "Workers")
public class Employee {
    @KeyField
    int id;

    String name;
    String position;

    public Employee () {

    }

    public Employee (int i, String n, String p) {
        id = i;
        name = n;
        position = p;
    }
}
