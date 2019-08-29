package sample;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Car {

  private String name;
  
  private int price;
  
  private int year;
  
  private int horsePower;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public int getPrice() {
    return price;
  }

  public void setPrice(int price) {
    this.price = price;
  }

  public int getYear() {
    return year;
  }

  public void setYear(int year) {
    this.year = year;
  }

  public int getHorsePower() {
    return horsePower;
  }

  public void setHorsePower(int horsePower) {
    this.horsePower = horsePower;
  }
}
