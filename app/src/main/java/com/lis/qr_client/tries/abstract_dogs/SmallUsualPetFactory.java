package com.lis.qr_client.tries.abstract_dogs;

public class SmallUsualPetFactory implements PetFactory{
    @Override
    public Cato createCat() {
        return new UsualCat();
    }

    @Override
    public Dogo createDog() {
        return new Pug();
    }
}
