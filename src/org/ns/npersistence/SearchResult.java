package org.ns.npersistence;

class SearchResult<T> {
	private boolean isPresent;
	private T field;
	public boolean isPresent() {
		return isPresent;
	}
	public void setPresent(boolean isPresent) {
		this.isPresent = isPresent;
	}
	public T getField() {
		return field;
	}
	public void setField(T field) {
		this.field = field;
	}
}
