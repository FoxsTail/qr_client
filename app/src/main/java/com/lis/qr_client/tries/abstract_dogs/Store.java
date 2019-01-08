package com.lis.qr_client.tries.abstract_dogs;

public class Store {
    public static void main(String[] args) {
        PetFactory petFactory = new SmallUsualPetFactory();

        Cato cat = petFactory.createCat();
        Dogo dog = petFactory.createDog();

        dog.bark();
        cat.meow();
    }
}
