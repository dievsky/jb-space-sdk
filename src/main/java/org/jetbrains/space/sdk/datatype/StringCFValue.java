package org.jetbrains.space.sdk.datatype;

public class StringCFValue extends CFValue {

    public StringCFValue(String value) {
        super("StringCFValue", value, null);
    }

    @Override
    public String getValue() {
        return (String) value;
    }

}
