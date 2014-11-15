package com.example.communityanimator.database;

public class Categories {
  
 String name = null;
 boolean selected = false;
  
 public Categories(String name, boolean selected) {
  super();
  this.name = name;
  this.selected = selected;
 }
   
 public String getName() {
  return name;
 }
 public void setName(String name) {
  this.name = name;
 }
 
 public boolean isSelected() {
  return selected;
 }
 public void setSelected(boolean selected) {
  this.selected = selected;
 }
  
}