package curta.function;

import curta.Function;

import java.util.List;

public class GetExponent extends Function {

    public GetExponent() {
        super("getExponent");
    }

    @Override
    public Object eval(Object... params) {

        super.checkNumberOfParams(1, 1, params);

        return Math.getExponent(super.getNumber(0, params));
    }
}