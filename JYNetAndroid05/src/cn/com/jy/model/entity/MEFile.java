package cn.com.jy.model.entity;

public class MEFile {
	private String name;
	private String path;
	public MEFile(String name, String path) {
		this.name = name;
		this.path = path;
	}
	public MEFile() {
		super();
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
}
