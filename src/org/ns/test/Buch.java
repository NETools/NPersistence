package org.ns.test;

import org.ns.npersistence.annotations.AtomicAttribute;
import org.ns.npersistence.annotations.PrimaryKey;
import org.ns.npersistence.annotations.Table;

@Table
public class Buch {
	
	@PrimaryKey
	private String isbn;
	
	@AtomicAttribute
	private String titel;
	
	public String getIsbn() {
		return isbn;
	}

	public void setIsbn(String isbn) {
		this.isbn = isbn;
	}

	public String getTitel() {
		return titel;
	}

	public void setTitel(String titel) {
		this.titel = titel;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	@AtomicAttribute
	private String author;
}
