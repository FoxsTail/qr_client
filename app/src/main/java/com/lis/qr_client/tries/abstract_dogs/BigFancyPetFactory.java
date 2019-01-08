package com.lis.qr_client.tries.abstract_dogs;

public class BigFancyPetFactory implements PetFactory{
    @Override
    public Cato createCat() {
        return new FancyCat();
    }

    @Override
    public Dogo createDog() {
        return new Senbernar();
    }
}
