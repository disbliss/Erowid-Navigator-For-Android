package org.erowid.navigatorandroid.xmlXstream;

import com.thoughtworks.xstream.annotations.XStreamImplicit;

import java.util.ArrayList;
import java.util.List;

public class ErowidPsychoactiveVaults {

    @XStreamImplicit
    protected List<Section> section;

    public List<Section> getSection() {
        if (section == null) {
            section = new ArrayList<Section>();
        }
        return this.section;
    }

}
