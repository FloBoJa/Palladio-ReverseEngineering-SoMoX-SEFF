package org.palladiosimulator.somox.ast2seff.casestudy.jobtest;

public class SimpleExternalClass {
    public void externalCallMethod(SimpleClass externalClass) {
        externalClass.ifAndElseMethod(true);
        externalClass.ifAndElseIfMethod(false, true);
        externalClass.whileMethod();
        this.internalCallMethod();
    }

    public void externalCallBoolean(SimpleClass externalClass) {
        externalClass.ifAndElseMethod(true);
    }

    public void externalCallString(SimpleClass externalClass) {
        externalClass.switchMethod("third");
    }

    public void externalCallChar(SimpleClass externalClass) {
        externalClass.charMethodBoolReturn('a');
        boolean returnValue = externalClass.charMethodBoolReturn('a');
        if (returnValue) {
            System.out.println("return worked");
        }
    }

    private void internalCallMethod() {
        System.out.println("just some logging");
    }
}