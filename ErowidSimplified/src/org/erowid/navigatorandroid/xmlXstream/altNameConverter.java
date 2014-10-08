package org.erowid.navigatorandroid.xmlXstream;
import com.thoughtworks.xstream.converters.extended.NamedArrayConverter;
import com.thoughtworks.xstream.mapper.Mapper;

/**
 * Created by quartz on 8/21/14.
 */
public class altNameConverter extends NamedArrayConverter {

    public altNameConverter(Class arrayType, Mapper mapper, String itemName) {
        super(arrayType, mapper, itemName);
    }

}
