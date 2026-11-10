/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

import java.time.LocalDate;

/**
 *
 * @author ASUS
 */
public class User {

	private int id;
	private int roleId;
	private String username;
	private String passwordHash;
	private String email;
	private String fullName;
	private String status;
	private String imageUrl;
	private LocalDate createdAt;

	public User() {

	}

	public User(int roleId, String username, String passwordHash) {
		this.roleId = roleId;
		this.username = username;
		this.passwordHash = passwordHash;
	}

	public User(int id, int roleId, String username, String passwordHash, String email, String fullName, String status, LocalDate createdAt) {
		this.id = id;
		this.roleId = roleId;
		this.username = username;
		this.passwordHash = passwordHash;
		this.email = email;
		this.fullName = fullName;
		this.status = status;
		this.createdAt = createdAt;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getRoleId() {
		return roleId;
	}

	public void setRoleId(int roleId) {
		this.roleId = roleId;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPasswordHash() {
		return passwordHash;
	}

	public void setPasswordHash(String passwordHash) {
		this.passwordHash = passwordHash;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getImageUrl() {
		return imageUrl;
	}

	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}

	public LocalDate getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDate createdAt) {
		this.createdAt = createdAt;
	}

}
