package app.xml;

import lombok.Getter;

//import java.lang.annotation.ElementType;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;


/**
 * Xml-элемент. Элемент состоит из имени, списка атрибутов({@link Attribute})
 *  и типа элемента({@link ElementType}).
 */
@Getter
public class Element {

    /**
     * имя элемента
     */
    private String name;
    /**
     * набор атрибутов
     */
    private List<Attribute> attributes = new ArrayList<>();
    /**
     * тип элемента
     */
    private ElementType type;

    public Element(String name, List<Attribute> attributes, ElementType type) {
        this.name = name;
        this.attributes = attributes;
        this.type = type;
    }

    /**
     * Фильтрует атрибуты элемента по заданному правилу.
     * @param predicate - правило фильтрации
     * @return отфильтрованный список атрибутов
     */
    public List<Attribute> getAttributesBy(Predicate<Attribute> predicate){
        return attributes.stream()
                .filter(predicate)
                .collect(Collectors.toList());
    }

    /**
     * Ищет атрибут по заданному правилу.
     * @param predicate - правило фильтрации
     * @return первый найденный атрибут
     */
    public Optional<Attribute> getAttributeBy(Predicate<Attribute> predicate){
        return attributes.stream()
                .filter(predicate)
                .findFirst();
    }

    @Override
    public String toString() {
        List<String> attributeStrings = attributes.stream()
                .map(Attribute::toString)
                .collect(Collectors.toList());

        return String.format("Element:{name = %s, attrs: %s}", name, String.join(" ", attributeStrings));
    }

    /**
     * Сравнивает элементы по атрибутам, имени и типу.
     * @param o - другой объект
     * @return true - равны, false - иначе.
     */
    @Override
    public boolean equals(Object o) {
        if(o == null){
            return false;
        }
        if(o.getClass() != this.getClass()){
            return false;
        }
        Element other = (Element) o;
        if(Objects.equals(attributes, other.attributes)){
            return true;
        }
        if(other.attributes.size() != this.attributes.size()){
            return false;
        }
        for(int i = 0; i < attributes.size(); ++i){
            if(!Objects.equals(other.attributes.get(i), attributes.get(i))){
                return false;
            }
        }
        return Objects.equals(other.name, name)
                && type == other.type;
    }

    public boolean isClose() {
        return type == ElementType.CLOSE;
    }

}
