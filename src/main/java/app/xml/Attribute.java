package app.xml;

import lombok.Getter;

import java.util.function.Predicate;

/**
 *  Атрибут xml-элемента. Он состоит из пары ключ-значение.
 */
@Getter
public class Attribute {

    private String name;
    private String value;

    /**
     * Возвращает предикат для фильтрации атрибутов по ключу.
     * @param name - ключ для фильтрации
     * @return предикат для фильтрации
     */
    public static Predicate<Attribute> filterByName(String name){
        return (attr) -> attr.name.equals(name);
    }

    public Attribute(String name, String value) {
//        validateName(name);
        this.name = name.trim();
        this.value = value;
    }

//    private void validateName(String name){
//        if(name == null || name.isBlank()){
//            throw new IllegalArgumentException("Attribute name must not be empty.");
//        }
//        if(!Character.isLetter(name.charAt(0))){
//            throw new IllegalArgumentException("Attribute name must start with alphabetic character.");
//        }
//    }

    @Override
    public String toString(){
        return String.format("Attribute: %s=\"%s\"", name, value);
    }

    @Override
    public boolean equals(Object o){
        if(o == null){
            return false;
        }
        if(o.getClass() != this.getClass()){
            return false;
        }
        Attribute other = (Attribute) o;
        return other.getName().equals(name) && other.getValue().equals(value);
    }
}
