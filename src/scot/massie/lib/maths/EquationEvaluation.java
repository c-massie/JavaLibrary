package scot.massie.lib.maths;

import org.assertj.core.util.Lists;
import scot.massie.lib.utils.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.ToDoubleFunction;

public final class EquationEvaluation
{
    //region Inner classes
    //region Pairings
    private static final class FunctionNameAndArgumentString
    {
        public FunctionNameAndArgumentString(String functionName, String argumentString)
        {
            this.functionName = functionName;
            this.argumentString = argumentString;
        }

        String functionName;
        String argumentString;

        public String getFunctionName()
        { return functionName; }

        public String getArgumentString()
        { return argumentString; }
    }

    private static final class StringAndPosition
    {
        public StringAndPosition(String s, int pos)
        {
            this.string = s;
            this.position = pos;
        }

        public final String string;
        public final int position;
    }
    //endregion

    //region Exceptions
    public static class UnparsableEquationException extends RuntimeException
    {
        public UnparsableEquationException(String fullEquation, String equationSection)
        {
            super("Equation was not parsable as an equation: " + fullEquation +
                  "\nSpecifically, this portion: " + equationSection);

            this.fullEquation = fullEquation;
            this.equationSection = equationSection;
        }

        UnparsableEquationException(String fullEquation, String equationSection, String msg)
        {
            super(msg);
            this.fullEquation = fullEquation;
            this.equationSection = equationSection;
        }

        final String equationSection;
        final String fullEquation;

        public String getFullEquation()
        { return fullEquation; }

        public String getEquationSection()
        { return equationSection; }

        public UnparsableEquationException withFullEquation(String fullEquation)
        { return new UnparsableEquationException(fullEquation, equationSection); }
    }

    public static class TrailingOperatorException extends UnparsableEquationException
    {
        public TrailingOperatorException(String fullEquation, String equationSection, boolean operatorIsAtEnd)
        {
            super(fullEquation,
                  equationSection,
                  "Equation was not parsable as an equation: " + fullEquation
                  + "\nSpecifically, this portion, which "
                  + (operatorIsAtEnd ? "ended with a non-postfix" : "started with a non-prefix") + " operator: "
                  + equationSection);

            this.operatorIsAtEnd = operatorIsAtEnd;
        }

        final boolean operatorIsAtEnd;

        public boolean operatorIsAtEnd()
        { return operatorIsAtEnd; }

        public boolean operatorIsAtStart()
        { return !operatorIsAtEnd; }

        public TrailingOperatorException withFullEquation(String fullEquation)
        { return new TrailingOperatorException(fullEquation, equationSection, operatorIsAtEnd); }
    }

    public static final class UnresolvedArgumentInEquationException extends RuntimeException
    {
        public UnresolvedArgumentInEquationException(String variableName)
        {
            super("Variable was unresolved: " + variableName);
            this.variableName = variableName;
        }

        private final String variableName;

        public String getVariableName()
        { return variableName; }
    }

    public static final class MissingFunctionException extends RuntimeException
    {
        public MissingFunctionException(String functionName)
        {
            super("Function was missing: " + functionName);
            this.functionName = functionName;
        }

        private final String functionName;

        public String getFunctionName()
        { return functionName; }
    }

    public static final class MissingFunctionArgumentsException extends RuntimeException
    {
        public MissingFunctionArgumentsException(String functionName, int numberOfArgsRequired, int numberOfArgsProvided)
        {
            super("The function \"" + functionName + "\" requires at least " + numberOfArgsRequired + " arguments, but"
                  + " only " + numberOfArgsProvided + " were provided.");

            this.functionName = functionName;
            this.numberOfArgsRequired = numberOfArgsRequired;
            this.numberOfArgsProvided = numberOfArgsProvided;
        }

        final String functionName;
        final int numberOfArgsRequired;
        final int numberOfArgsProvided;

        public String getFunctionName()
        { return functionName; }

        public int getNumberOfArgsRequired()
        { return numberOfArgsRequired; }

        public int getNumberOfArgsProvided()
        { return numberOfArgsProvided; }
    }
    //endregion

    //region Operator actions
    @FunctionalInterface
    public interface UnaryOperatorAction
    {
        double performOperation(double x);
    }

    @FunctionalInterface
    public interface BinaryOperatorAction
    {
        double performOperation(double x, double y);
    }
    //endregion

    //region misc
    private static class OperatorGroup
    {
        public OperatorGroup(double priority)
        { this.priority = priority; }

        double priority;

        List<BinaryOperator> leftAssociativeBinaryOperators = new ArrayList<>();
        List<BinaryOperator> rightAssociativeBinaryOperators = new ArrayList<>();
        List<UnaryOperator> prefixOperators = new ArrayList<>();
        List<UnaryOperator> suffixOperators = new ArrayList<>();

        public void addOperator(Operator op)
        {
            if(op instanceof BinaryOperator)
            {
                BinaryOperator bop = (BinaryOperator)op;

                if(bop.isLeftAssociative)
                    leftAssociativeBinaryOperators.add(bop);
                else
                    rightAssociativeBinaryOperators.add(bop);
            }
            else // op is UnaryOperator
            {
                UnaryOperator uop = (UnaryOperator)op;

                if(uop.isSuffixOperator)
                    suffixOperators.add(uop);
                else
                    prefixOperators.add(uop);
            }
        }

        public OperatorGroup withOp(Operator op)
        {
            addOperator(op);
            return this;
        }
    }
    //endregion

    //region Operators
    private static class Operator
    {
        public Operator(char lex, double priority)
        {
            this.lex = lex;
            this.priority = priority;
        }

        char lex;
        double priority;
    }

    private static class UnaryOperator extends Operator
    {
        public UnaryOperator(char lex, double priority, UnaryOperatorAction action, boolean isSuffixOperator)
        {
            super(lex, priority);
            this.action = action;
            this.isSuffixOperator = isSuffixOperator;
        }

        public UnaryOperator(char lex, double priority, UnaryOperatorAction action)
        { this(lex, priority, action, false); }

        UnaryOperatorAction action;
        boolean isSuffixOperator;
    }

    private static class BinaryOperator extends Operator
    {
        public BinaryOperator(char lex, double priority, BinaryOperatorAction action, boolean isLeftAssociative)
        {
            super(lex, priority);
            this.action = action;
            this.isLeftAssociative = isLeftAssociative;
        }

        public BinaryOperator(char lex, double priority, BinaryOperatorAction action)
        { this(lex, priority, action, true); }

        BinaryOperatorAction action;
        boolean isLeftAssociative;
    }
    //endregion

    //region Equation components
    private static abstract class EquationComponent
    {
        public abstract double evaluate();
    }

    private static class LiteralNumber extends EquationComponent
    {
        public LiteralNumber(double value)
        { this.value = value; }

        double value;

        @Override
        public double evaluate()
        { return value; }
    }

    private static class EquationVariable extends EquationComponent
    {
        public EquationVariable(Map<String, Double> variableValues, String name)
        {
            this.variableValues = variableValues;
            this.name = name;
        }

        Map<String, Double> variableValues;
        String name;

        @Override
        public double evaluate()
        {
            Double result = variableValues.get(name);

            if(result == null)
                throw new UnresolvedArgumentInEquationException(name);

            return result;
        }
    }

    private static abstract class Operation extends EquationComponent
    { }

    private static class BinaryOperation extends Operation
    {
        public BinaryOperation(EquationComponent l, EquationComponent r, BinaryOperatorAction action)
        {
            leftOperand = l;
            rightOperand = r;
            this.action = action;
        }

        public EquationComponent leftOperand, rightOperand;
        BinaryOperatorAction action;

        @Override
        public double evaluate()
        { return action.performOperation(leftOperand.evaluate(), rightOperand.evaluate()); }
    }

    private static class UnaryOperation extends Operation
    {
        public UnaryOperation(EquationComponent o, UnaryOperatorAction action)
        {
            this.operand = o;
            this.action = action;
        }

        public EquationComponent operand;
        UnaryOperatorAction action;

        @Override
        public double evaluate()
        { return action.performOperation(operand.evaluate()); }
    }

    private static abstract class VariadicOperation extends Operation
    {
        public VariadicOperation(EquationComponent... os)
        { operands = os; }

        public EquationComponent[] operands;
    }

    private static class FunctionCall extends VariadicOperation
    {
        public FunctionCall(Map<String, ToDoubleFunction<double[]>> functionMap,
                            String functionName,
                            EquationComponent... os)
        {
            super(os);
            this.functionName = functionName;
            this.functionMap = functionMap;
        }

        String functionName;
        Map<String, ToDoubleFunction<double[]>> functionMap;

        @Override
        public double evaluate()
        {
            ToDoubleFunction<double[]> f = functionMap.get(functionName);

            if(f == null)
                throw new MissingFunctionException(functionName);

            double[] results = new double[operands.length];

            for(int i = 0; i < results.length; i++)
                results[i] = operands[i].evaluate();

            return f.applyAsDouble(results);
        }
    }
    //endregion
    //endregion

    //region Initialisation
    public EquationEvaluation(String equation)
    {
        this.unprocessedEquation = equation;
        this.equation = preprocessEquation(equation);

        for(int i = 0; i < defaultOperators.length; i++)
            addOperatorDumbly(defaultOperators[i]);
    }
    //endregion

    //region Static variables and constants
    private static final double PHI = (1 + Math.sqrt(5)) / 2;

    private static final Operator[] defaultOperators =
    {
            new BinaryOperator('-', 100,  (x, y) -> x - y),
            new BinaryOperator('+', 100,  (x, y) -> x + y),
            new BinaryOperator('/', 200,  (x, y) -> x / y),
            new BinaryOperator('÷', 200,  (x, y) -> x / y),
            new BinaryOperator('*', 200,  (x, y) -> x * y),
            new BinaryOperator('×', 200,  (x, y) -> x * y),
            new BinaryOperator('%', 300,  (x, y) -> x % y),
            new UnaryOperator ('-', 500,  x -> -x),
            new UnaryOperator ('+', 500,  x -> x),
            new BinaryOperator('√', 600,  (x, y) -> Math.pow(y, 1.0 / x)),
            new UnaryOperator ('√', 700, x -> Math.sqrt(x)),
            new BinaryOperator('^', 800,  (x, y) -> Math.pow(x, y), false),
            new UnaryOperator ('%', 900, x -> x / 100, true)
    };
    //endregion

    //region Instance variables
    private final String unprocessedEquation;
    private final String equation;
    private EquationComponent topLevelComponent = null;

    private final Map<String, Double> variableValues = new HashMap<>();
    {
        variableValues.put("π", Math.PI);
        variableValues.put("pi", Math.PI);
        variableValues.put("e", Math.E);
        variableValues.put("ϕ", PHI);
        variableValues.put("φ", PHI);
        variableValues.put("phi", PHI);
        variableValues.put("∞", Double.POSITIVE_INFINITY);
        variableValues.put("inf", Double.POSITIVE_INFINITY);
    }

    private final Map<String, ToDoubleFunction<double[]>> functionMap = new HashMap<>();
    {
        functionMap.put("cos", args ->
        {
            if(args.length < 1)
                throw new MissingFunctionArgumentsException("cos", 1, args.length);

            return Math.cos(args[0]);
        });

        functionMap.put("sin", args ->
        {
            if(args.length < 1)
                throw new MissingFunctionArgumentsException("sin", 1, args.length);

            return Math.sin(args[0]);
        });

        functionMap.put("tan", args ->
        {
            if(args.length < 1)
                throw new MissingFunctionArgumentsException("tan", 1, args.length);

            return Math.tan(args[0]);
        });

        functionMap.put("sqrt", args ->
        {
            if(args.length < 1)
                throw new MissingFunctionArgumentsException("sqrt", 1, args.length);

            return Math.sqrt(args[0]);
        });

        functionMap.put("cbrt", args ->
        {
            if(args.length < 1)
                throw new MissingFunctionArgumentsException("cbrt", 1, args.length);

            return Math.cbrt(args[0]);
        });

        functionMap.put("log", args ->
        {
            if(args.length < 1)
                throw new MissingFunctionArgumentsException("log", 1, args.length);

            return Math.log(args[0]);
        });

        functionMap.put("log10", args ->
        {
            if(args.length < 1)
                throw new MissingFunctionArgumentsException("log10", 1, args.length);

            return Math.log10(args[0]);
        });

        functionMap.put("min", args ->
        {
            if(args.length < 1)
                throw new MissingFunctionArgumentsException("min", 1, args.length);

            double min = args[0];

            for(int i = 1; i < args.length; i++)
                if(args[i] < min)
                    min = args[i];

            return min;
        });

        functionMap.put("max", args ->
        {
            if(args.length < 1)
                throw new MissingFunctionArgumentsException("max", 1, args.length);

            double max = args[0];

            for(int i = 1; i < args.length; i++)
                if(args[i] > max)
                    max = args[i];

            return max;
        });

        functionMap.put("floor", args ->
        {
            if(args.length < 1)
                throw new MissingFunctionArgumentsException("floor", 1, args.length);

            return Math.floor(args[0]);
        });

        functionMap.put("ceiling", args ->
        {
            if(args.length < 1)
                throw new MissingFunctionArgumentsException("ceiling", 1, args.length);

            return Math.ceil(args[0]);
        });

        functionMap.put("ceil", args ->
        {
            if(args.length < 1)
                throw new MissingFunctionArgumentsException("ceil", 1, args.length);

            return Math.ceil(args[0]);
        });

        functionMap.put("truncate", args ->
        {
            if(args.length < 1)
                throw new MissingFunctionArgumentsException("truncate", 1, args.length);

            return (int)args[0];
        });

        functionMap.put("trunc", args ->
        {
            if(args.length < 1)
                throw new MissingFunctionArgumentsException("trunc", 1, args.length);

            return (int)args[0];
        });

        functionMap.put("round", args ->
        {
            if(args.length < 1)
                throw new MissingFunctionArgumentsException("round", 1, args.length);

            return Math.round(args[0]);
        });

        functionMap.put("avg", args ->
        {
            if(args.length < 1)
                throw new MissingFunctionArgumentsException("round", 1, args.length);

            double avg = args[0];

            for(int i = 1; i < args.length; i++)
                avg += (args[i] - avg) / (i + 1);

            return avg;
        });

        functionMap.put("median", args ->
        {
            if(args.length < 1)
                throw new MissingFunctionArgumentsException("median", 1, args.length);

            Arrays.sort(args);

            if(args.length % 2 == 0)
                return (args[args.length / 2] + args[args.length / 2 - 1]) / 2;
            else
                return args[args.length / 2];
        });

        functionMap.put("fib", args ->
        {
            if(args.length < 1)
                throw new MissingFunctionArgumentsException("fib", 1, args.length);

            double n = args[0];
            double result = (Math.pow(PHI, n) - (Math.pow(-PHI, -n))) / (Math.sqrt(5));
            return n % 1 == 0 ? Math.round(result) : result;
        });
    }

    private final List<OperatorGroup> operatorGroups = new ArrayList<>();

    private final Map<Character, BinaryOperator> binaryOperators = new HashMap<>();

    private final Map<Character, UnaryOperator> prefixOperators = new HashMap<>();

    private final Map<Character, UnaryOperator> suffixOperators = new HashMap<>();

    private final Set<Character> operatorChars = new HashSet<>();
    //endregion

    //region Methods
    //region Methods about state
    private boolean charIsNonPrefixOperator(char c)
    {
        for(char oc : operatorChars)
            if(c == oc)
                return !prefixOperators.containsKey(c);

        return false;
    }

    private boolean charIsNonSuffixOperator(char c)
    {
        for(char oc : operatorChars)
            if(c == oc)
                return !suffixOperators.containsKey(c);

        return false;
    }
    //endregion

    //region Internal operator registration
    private OperatorGroup getOrCreateOpGroup(double priority)
    {
        for(OperatorGroup og : operatorGroups)
            if(og.priority == priority)
                return og;

        OperatorGroup newOg = new OperatorGroup(priority);
        int p = -(Collections.binarySearch(operatorGroups, newOg, Comparator.comparingDouble(a -> a.priority)) + 1);
        operatorGroups.add(p, newOg);
        return newOg;
    }

    private void removeBinaryOpFromOpGroupsIfRegistered(BinaryOperator op)
    {
        BinaryOperator existingOp = binaryOperators.get(op.lex);

        if(existingOp != null)
        {
            for(OperatorGroup og : operatorGroups)
            {
                if(og.leftAssociativeBinaryOperators.remove(existingOp))
                    break;

                if(og.rightAssociativeBinaryOperators.remove(existingOp))
                    break;
            }
        }
    }

    private void removePrefixOpFromOpGroupsIfRegistered(UnaryOperator op)
    {
        UnaryOperator existingOp = prefixOperators.get(op.lex);

        if(existingOp != null)
            for(OperatorGroup og : operatorGroups)
                if(og.prefixOperators.remove(existingOp))
                    break;
    }

    private void removeSuffixOpFromOpGroupsIfRegistered(UnaryOperator op)
    {
        UnaryOperator existingOp = prefixOperators.get(op.lex);

        if(existingOp != null)
            for(OperatorGroup og : operatorGroups)
                if(og.suffixOperators.remove(existingOp))
                    break;
    }

    private void addOperator(Operator op)
    {
        if(op instanceof BinaryOperator)
        {
            removeBinaryOpFromOpGroupsIfRegistered((BinaryOperator)op);
            binaryOperators.put(op.lex, (BinaryOperator)op);
        }
        else
        {
            UnaryOperator uop = (UnaryOperator)op;

            if(uop.isSuffixOperator)
            {
                removeSuffixOpFromOpGroupsIfRegistered(uop);
                suffixOperators.put(uop.lex, uop);
            }
            else
            {
                removePrefixOpFromOpGroupsIfRegistered(uop);
                prefixOperators.put(uop.lex, uop);
            }
        }

        getOrCreateOpGroup(op.priority).addOperator(op);
        operatorChars.add(op.lex);
    }

    private void addOperatorDumbly(Operator op)
    {
        if(op instanceof BinaryOperator)
            binaryOperators.put(op.lex, (BinaryOperator)op);
        else
        {
            UnaryOperator uop = (UnaryOperator)op;
            (uop.isSuffixOperator ? suffixOperators : prefixOperators).put(uop.lex, uop);
        }

        getOrCreateOpGroup(op.priority).addOperator(op);
        operatorChars.add(op.lex);
    }
    //endregion

    //region Parsing
    private static String preprocessEquation(String possibleEquation)
    {
        // TO DO: Write function to replace superscript numbers grouping them together, so "x²³" becomes "x^23" rather
        // than "x^2^3"
        possibleEquation = possibleEquation.replaceAll("⁰", "^0");
        possibleEquation = possibleEquation.replaceAll("¹", "^1");
        possibleEquation = possibleEquation.replaceAll("²", "^2");
        possibleEquation = possibleEquation.replaceAll("³", "^3");
        possibleEquation = possibleEquation.replaceAll("⁴", "^4");
        possibleEquation = possibleEquation.replaceAll("⁵", "^5");
        possibleEquation = possibleEquation.replaceAll("⁶", "^6");
        possibleEquation = possibleEquation.replaceAll("⁷", "^7");
        possibleEquation = possibleEquation.replaceAll("⁸", "^8");
        possibleEquation = possibleEquation.replaceAll("⁹", "^9");

        return possibleEquation;
    }

    private EquationComponent parse(String possibleEquation)
    {
        possibleEquation = possibleEquation.trim();

        if(possibleEquation.isEmpty())
            throw new UnparsableEquationException(possibleEquation, possibleEquation);

        if(charIsNonPrefixOperator(possibleEquation.charAt(0)))
            throw new TrailingOperatorException(possibleEquation, possibleEquation, false);

        if(charIsNonSuffixOperator(possibleEquation.charAt(possibleEquation.length() - 1)))
            throw new TrailingOperatorException(possibleEquation, possibleEquation, true);

        if((possibleEquation.charAt(0) == '(')
           && (StringUtils.getMatchingBracketPosition(possibleEquation, 0) == possibleEquation.length() - 1))
        {
            return parse(possibleEquation.substring(1, possibleEquation.length() - 1));
        }

        Operation o = parseOperation(possibleEquation);

        if(o != null)
            return o;

        if(variableValues.containsKey(possibleEquation))
            return new EquationVariable(variableValues, possibleEquation);

        FunctionCall call = parseFunctionCall(possibleEquation);

        if(call != null)
            return call;

        try
        {
            double literalNumberValue = Double.parseDouble(possibleEquation);
            return new LiteralNumber(literalNumberValue);
        }
        catch(NumberFormatException e)
        { }

        throw new UnparsableEquationException(possibleEquation, possibleEquation);
    }

    private Operation parseOperation(String s)
    {
        for(OperatorGroup og : operatorGroups)
        {
            Operation o = parseOperation(s, og);

            if(o != null)
                return o;
        }

        return null;
    }

    private Operation parseOperation(String s, OperatorGroup og)
    {
        if(!og.rightAssociativeBinaryOperators.isEmpty())
        {
            Operation o = parseOperation_rightAssociative(s, og);

            if(o != null)
                return o;
        }

        if(!og.leftAssociativeBinaryOperators.isEmpty())
        {
            Operation o = parseOperation_leftAssociative(s, og);

            if(o != null)
                return o;
        }

        if(!og.suffixOperators.isEmpty())
        {
            Operation o = parseOperation_suffix(s, og);

            if(o != null)
                return o;
        }

        if(!og.prefixOperators.isEmpty())
        {
            Operation o = parseOperation_prefix(s, og);

            if(o != null)
                return o;
        }

        return null;
    }

    private Operation parseOperation_leftAssociative(String s, OperatorGroup og)
    {
        int bracketDepth = 0;
        int slength = s.length();

        for(int i = slength - 1; i >= 0; i--)
        {
            char ichar = s.charAt(i);

            if(ichar == ')')
                bracketDepth++;
            else if(ichar == '(')
            {
                if(--bracketDepth < 0)
                    return null;
            }
            else if(bracketDepth == 0)
            {
                if(i == 0 || i == slength - 1)
                    continue;

                for(BinaryOperator op : og.leftAssociativeBinaryOperators)
                {
                    if(ichar == op.lex)
                    {
                        if(!canBeBinaryOperator(s, i))
                            break;

                        return new BinaryOperation(parse(s.substring(0, i)), parse(s.substring(i + 1)), op.action);
                    }
                }
            }
        }

        return null;
    }

    private Operation parseOperation_rightAssociative(String s, OperatorGroup og)
    {
        int bracketDepth = 0;
        int slength = s.length();

        for(int i = 0; i < slength; i++)
        {
            char ichar = s.charAt(i);

            if(ichar == '(')
                bracketDepth++;
            else if(ichar == ')')
            {
                if(--bracketDepth < 0)
                    return null;
            }
            else if(bracketDepth == 0)
            {
                if(i == 0 || i == slength - 1)
                    continue;

                for(BinaryOperator op : og.rightAssociativeBinaryOperators)
                {
                    if(ichar == op.lex)
                    {
                        if(!canBeBinaryOperator(s, i))
                            break;

                        return new BinaryOperation(parse(s.substring(0, i)), parse(s.substring(i + 1)), op.action);
                    }
                }
            }
        }

        return null;
    }

    private Operation parseOperation_prefix(String s, OperatorGroup og)
    {
        for(UnaryOperator uop : og.prefixOperators)
            if(s.charAt(0) == uop.lex)
                return new UnaryOperation(parse(s.substring(1)), uop.action);

        return null;
    }

    private Operation parseOperation_suffix(String s, OperatorGroup og)
    {
        int lastPos = s.length() - 1;

        for(UnaryOperator uop : og.suffixOperators)
            if(s.charAt(lastPos) == uop.lex)
                return new UnaryOperation(parse(s.substring(0, lastPos)), uop.action);

        return null;
    }

    private boolean canBeBinaryOperator(String s, int pos)
    {
        StringAndPosition opRun = getOpRunForBinaryOp(s, pos);

        if(opRun == null)
            return false;

        for(int i = opRun.position - 1; i >= 0; i--)
        {
            char ichar = opRun.string.charAt(i);

            if((ichar != ' ') && (!suffixOperators.containsKey(ichar)))
                return false;
        }

        for(int i = opRun.position + 1; i < opRun.string.length(); i++)
        {
            char ichar = opRun.string.charAt(i);

            if((ichar != ' ') && (!prefixOperators.containsKey(ichar)))
                return false;
        }

        return true;
    }

    private StringAndPosition getOpRunForBinaryOp(String s, int opPosition)
    {
        // From and to are the min and max positions (both inclusive) of the operator run in the provided string.
        int from = opPosition - 1;
        int to = opPosition + 1;

        for(char c = s.charAt(from); (c == ' ') || (operatorChars.contains(c)); c = s.charAt(--from))
        {
            // If the op run stretches to the start of the string, it's not a valid op run for a binary operator.
            if(from == 0)
                return null;
        }

        from++;

        for(char c = s.charAt(to); (c == ' ') || (operatorChars.contains(c)); c = s.charAt(++to))
        {
            // If the op run stretches to the end of the string, it's not a valid op run for a binary operator.
            if(to == s.length() - 1)
                return null;
        }

        to--;

        String opRun = s.substring(from, to + 1);
        return new StringAndPosition(opRun, opPosition - from);
    }

    /**
     * Gets the name of the function represented by the given string, paired with the string passed into it as
     * arguments.
     * @param s The string representation of a function call.
     * @return The name of the function being called paired with a string representation of the arguments passed into
     *         it, or null if the given string is not the representation of a function being called.
     */
    private static FunctionNameAndArgumentString getFunctionNameAndArgumentString(String s)
    {
        int openingBracketPosition = -1;
        int slength = s.length();

        for(int i = 0; i < slength; i++)
        {
            if(s.charAt(i) == '(')
            {
                openingBracketPosition = i;
                break;
            }
        }

        if(openingBracketPosition <= 0) // no bracket, or bracket is first character.
            return null;

        int closingBracketPosition = StringUtils.getMatchingBracketPosition(s, openingBracketPosition);

        if(closingBracketPosition != (slength - 1))
            return null;

        String functionName = s.substring(0, openingBracketPosition);
        String argumentString = s.substring(openingBracketPosition + 1, closingBracketPosition);
        return new FunctionNameAndArgumentString(functionName, argumentString);
    }

    private static List<String> splitFunctionArgString(String argString)
    {
        argString = argString.trim();

        if(argString.startsWith(",") || argString.endsWith(","))
            return null;

        if(argString.isEmpty())
            return Lists.emptyList();

        int argStringLength = argString.length();
        int bracketDepth = 0;
        int lastSplit = -1;
        List<String> arguments = new ArrayList<>();

        for(int i = 0; i < argStringLength; i++)
        {
            char ichar = argString.charAt(i);

            if(ichar == '(')
                bracketDepth++;
            else if(ichar == ')')
            {
                if(--bracketDepth < 0)
                    return null;
            }
            else if(ichar == ',')
            {
                arguments.add(argString.substring(lastSplit + 1, i).trim());
                lastSplit = i;
            }
        }

        arguments.add(argString.substring(lastSplit + 1));
        return arguments;
    }

    private FunctionCall parseFunctionCall(String functionCallString)
    {
        FunctionNameAndArgumentString fAndA = getFunctionNameAndArgumentString(functionCallString);

        if(fAndA == null || !functionMap.containsKey(fAndA.getFunctionName()))
            return null;

        List<String> arguments = splitFunctionArgString(fAndA.getArgumentString());

        if(arguments == null)
            return null;

        EquationComponent[] parsedArguments = new EquationComponent[arguments.size()];

        for(int i = 0; i < parsedArguments.length; i++)
            parsedArguments[i] = parse(arguments.get(i));

        return new FunctionCall(functionMap, fAndA.getFunctionName(), parsedArguments);
    }
    //endregion

    //region Public interface
    public EquationEvaluation build()
    {
        try
        { topLevelComponent = parse(equation); }
        catch(UnparsableEquationException ex)
        { throw ex.withFullEquation(equation); }
        return this;
    }

    public double evaluate()
    {
        if(topLevelComponent == null)
            build();

        return topLevelComponent.evaluate();
    }

    public EquationEvaluation withVariable(String argumentRepresentedBy, double argumentValue)
    {
        variableValues.put(argumentRepresentedBy, argumentValue);
        return this;
    }

    public EquationEvaluation withFunction(String functionName, ToDoubleFunction<double[]> f)
    {
        functionMap.put(functionName, f);
        return this;
    }

    public EquationEvaluation withOperator(char operatorCharacter,
                                           double precedenceLevel,
                                           BinaryOperatorAction calculation)
    {
        addOperator(new BinaryOperator(operatorCharacter, precedenceLevel, calculation));
        return this;
    }

    // Defaults the operator precedence to 0, or 100 below addition/subtraction.
    public EquationEvaluation withOperator(char operatorCharacter,
                                           BinaryOperatorAction calculation)
    { return withOperator(operatorCharacter, 0, calculation); }

    public EquationEvaluation withRightAssociativeOperator(char operatorCharacter,
                                                           double precedenceLevel,
                                                           BinaryOperatorAction calculation)
    {
        addOperator(new BinaryOperator(operatorCharacter, precedenceLevel, calculation, false));
        return this;
    }

    // Defaults the operator precedence to 0, or 100 below addition/subtraction.
    public EquationEvaluation withRightAssociativeOperator(char operatorCharacter,
                                                           BinaryOperatorAction calculation)
    { return withRightAssociativeOperator(operatorCharacter, 0, calculation); }

    public EquationEvaluation withPrefixOperator(char operatorCharacter,
                                                 double precedenceLevel,
                                                 UnaryOperatorAction calculation)
    {
        addOperator(new UnaryOperator(operatorCharacter, precedenceLevel, calculation, false));
        return this;
    }

    // Defaults the operator precedence to 400, or 100 below the positive/negative unary operators
    public EquationEvaluation withPrefixOperator(char operatorCharacter,
                                                 UnaryOperatorAction calculation)
    {
        addOperator(new UnaryOperator(operatorCharacter, 4, calculation, false));
        return this;
    }

    public EquationEvaluation withSuffixOperator(char operatorCharacter,
                                                 double precedenceLevel,
                                                 UnaryOperatorAction calculation)
    {
        addOperator(new UnaryOperator(operatorCharacter, precedenceLevel, calculation, true));
        return this;
    }

    // Defaults the operator precedence to 400, or 100 below the positive/negative unary operators
    public EquationEvaluation withSuffixOperator(char operatorCharacter,
                                                 UnaryOperatorAction calculation)
    {
        addOperator(new UnaryOperator(operatorCharacter, 4, calculation, true));
        return this;
    }
    //endregion
    //endregion
}
