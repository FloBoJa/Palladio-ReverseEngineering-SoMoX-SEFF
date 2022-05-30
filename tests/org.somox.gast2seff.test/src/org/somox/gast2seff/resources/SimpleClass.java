package org.somox.gast2seff.resources;

public class SimpleClass {

	public void ifMethod() {
		System.out.println("Hello World!");
		
		if (true) {
			System.out.println("Inside the ifStatement.");
		}
	}
	
	public void forMethod() {
		System.out.println("Hello World!");
		
		for (int i = 0; i < 10; i++) {
			System.out.println("Current position: " + i);
		}

	}
	
	public void whileMethod() {
		System.out.println("Hello World!");
		
		int i = 0;
		boolean work = true;
		while (work) {
			System.out.println("Current position: " + i);
			work = false;
		}

	}
	
	public void ifAndForMethod() {
		System.out.println("Hello World!");
		
		if (true) {
			System.out.println("Inside the ifStatement.");
			
			for (int i = 0; i < 10; i++) {
				System.out.println("Current position: " + i);
			}
			
		}
	}
	
	public void ifAndElseMethod(boolean decision) {
		System.out.println("Hello World!");
				
		if (decision) {
			System.out.println("Inside the ifStatement.");
		} else {
			System.out.println("Inside the elseStatement.");
		}
	}
	
	public void ifAndElseIfMethod(boolean decision, boolean decision2) {
		System.out.println("Hello World!");
				
		if (decision) {
			System.out.println("Inside the ifStatement.");
		} else if (decision2) {
			System.out.println("Inside the elseIfStatement.");
		} else {
			System.out.println("Inside the elseStatement.");
		}
	}
	
}
