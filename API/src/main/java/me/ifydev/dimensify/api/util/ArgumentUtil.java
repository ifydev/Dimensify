package me.ifydev.dimensify.api.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Innectic
 * @since 10/1/2017
 */
public class ArgumentUtil {

    /**
     * Get the remaining arguments from a list from a point on
     *
     * @param starting the starting point
     * @param arguments the arguments to get from
     * @return the remaining arguments
     */
    public static String[] getRemainingArgs(int starting, String[] arguments) {
        List<String> args = new ArrayList<>(Arrays.asList(arguments));
        return args.subList(starting, args.size()).toArray(new String[]{});
    }
}
