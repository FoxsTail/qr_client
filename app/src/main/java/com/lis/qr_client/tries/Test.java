package com.lis.qr_client.tries;

import com.lis.qr_client.pojo.Inventory;

import java.util.ArrayList;
import java.util.List;

public class Test {
    public static void main(String[] args) {
    /*    Testo testo = new Testo();
        testo.addB(7);
        System.out.println("a testo "+testo.getA());
        System.out.println("b testo "+testo.getB());

        Child child = new Child();
        child.addB(9);
        System.out.println("a child "+child.getA());
        System.out.println("b child "+child.getB());
*/
    int chosen_address = 7;
        List<Integer> loaded_addresses = new ArrayList<>();

        System.out.println(loaded_addresses.indexOf(chosen_address));
        if(loaded_addresses.indexOf(chosen_address) > 0){
            loaded_addresses.add(chosen_address);
        }
    }

}

class Testo {

private int a = 2;
private int b;

    public Testo() {

    }

    public void addB(int b){
        this.b = b+10;
    }

    public int getA() {
        return a;
    }

    public int getB() {
        return b;
    }

    public void setA(int a) {
        this.a = a;
    }

    public void setB(int b) {
        this.b = b;
    }
}

class Child extends Testo{
    @Override
    public void addB(int b) {
       setB(b+90);
    }


}


