package mod.chloeprime.gunsmithlib.common.scripting.lang;

import org.luaj.vm2.Varargs;

import java.awt.*;

final class ColorParser {
    static Color newColor(Varargs args) {
        var sepChannel = args.narg() >= 3;
        if (sepChannel) {
            double r = args.todouble(1);
            double g = args.todouble(2);
            double b = args.todouble(3);
            double rawA = args.optdouble(4, Double.NaN);
            boolean isIntColor = isInt(r) && isInt(g) && isInt(b) && (Double.isNaN(rawA) || isInt(rawA));
            double a = Double.isNaN(rawA) ? (isIntColor ? 255 : 1) : rawA;
            return isIntColor
                    ? new Color(roundColor(r), roundColor(g), roundColor(b), roundColor(a))
                    : new Color((float) r, (float) g, (float) b, (float) a);
        } else {
            double alpha;
            if (args.isnumber(2)) {
                double arg2 = args.checkdouble(2);
                alpha = isInt(arg2) ? arg2 / 255.0 : arg2;
            } else {
                alpha = 1;
            }
            var rgb = Color.decode(args.checkjstring(1)).getRGBColorComponents(COLOR_BUFFER.get());
            return new Color(rgb[0], rgb[1], rgb[2], (float) alpha);
        }
    }

    private static final ThreadLocal<float[]> COLOR_BUFFER = ThreadLocal.withInitial(() -> new float[3]);

    private static boolean isInt(double number) {
        if (!Double.isFinite(number)) {
            return false;
        }
        return (number - Math.round(number)) <= 1e-8;
    }

    private static int roundColor(double color) {
        return (int) Math.round(color);
    }

    private ColorParser() {
    }
}
