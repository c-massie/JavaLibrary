package scot.massie.lib.maths;

import org.assertj.core.util.Lists;
import scot.massie.lib.utils.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.ToDoubleFunction;

public final class EquationEvaluation
{
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
    {
        public Operation(double priority)
        { this.priority = priority; }

        double priority;
    }

    private static abstract class BinaryOperation extends Operation
    {
        public BinaryOperation(EquationComponent l, EquationComponent r, double priority)
        {
            super(priority);
            leftOperand = l;
            rightOperand = r;
        }

        public BinaryOperation(EquationComponent l, EquationComponent r)
        { this(l, r, 0); }

        public EquationComponent leftOperand, rightOperand;
    }

    private static abstract class UnaryOperation extends Operation
    {
        public UnaryOperation(EquationComponent o, double priority)
        {
            super(priority);
            operand = o;
        }

        public UnaryOperation(EquationComponent o)
        { this(o, 0); }

        public EquationComponent operand;
    }

    private static abstract class VariadicOperation extends EquationComponent
    {
        public VariadicOperation(EquationComponent... os)
        { operands = os; }

        public EquationComponent[] operands;
    }

    private static class Addition extends BinaryOperation
    {
        public Addition(EquationComponent l, EquationComponent r)
        { super(l, r); }

        @Override
        public double evaluate()
        { return leftOperand.evaluate() + rightOperand.evaluate(); }
    }

    private static class Subtraction extends BinaryOperation
    {
        public Subtraction(EquationComponent l, EquationComponent r)
        { super(l, r); }

        @Override
        public double evaluate()
        { return leftOperand.evaluate() - rightOperand.evaluate(); }
    }

    private static class Multiplication extends BinaryOperation
    {
        public Multiplication(EquationComponent l, EquationComponent r)
        { super(l, r); }

        @Override
        public double evaluate()
        { return leftOperand.evaluate() * rightOperand.evaluate(); }
    }

    private static class Division extends BinaryOperation
    {
        public Division(EquationComponent l, EquationComponent r)
        { super(l, r); }

        @Override
        public double evaluate()
        { return leftOperand.evaluate() / rightOperand.evaluate(); }
    }

    private static class Exponent extends BinaryOperation
    {
        public Exponent(EquationComponent l, EquationComponent r)
        { super(l, r); }

        @Override
        public double evaluate()
        { return Math.pow(leftOperand.evaluate(), rightOperand.evaluate()); }
    }

    private static class Root extends BinaryOperation
    {
        public Root(EquationComponent l, EquationComponent r)
        { super(l, r); }

        @Override
        public double evaluate()
        { return Math.pow(rightOperand.evaluate(), 1.0 / leftOperand.evaluate()); }
    }

    private static class Negation extends UnaryOperation
    {
        public Negation(EquationComponent o)
        { super(o); }

        @Override
        public double evaluate()
        { return -(operand.evaluate()); }
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

    public EquationEvaluation(String equation)
    {
        this.unprocessedEquation = equation;
        this.equation = preprocessEquation(equation);
    }

    private final String unprocessedEquation;
    private final String equation;
    private EquationComponent topLevelComponent = null;

    private final Map<String, Double> variableValues = new HashMap<>();
    {
        double phi = (1 + Math.sqrt(5)) / 2;

        variableValues.put("π", Math.PI);
        variableValues.put("pi", Math.PI);
        variableValues.put("e", Math.E);
        variableValues.put("ϕ", phi);
        variableValues.put("φ", phi);
        variableValues.put("phi", phi);
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

            //return Math.(args[0]);
            return (int)args[0];
        });

        functionMap.put("trunc", args ->
        {
            if(args.length < 1)
                throw new MissingFunctionArgumentsException("trunc", 1, args.length);

            //return Math.(args[0]);
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
    }

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

        String[] notUnaryPrefixOperators = { "*", "×", "/", "÷", "^" };
        String[] notUnarySuffixOperators = { "+", "-", "*", "×", "/", "÷", "^", "√" };

        for(int i = 0; i < notUnaryPrefixOperators.length; i++)
            if(possibleEquation.startsWith(notUnaryPrefixOperators[i]))
                throw new TrailingOperatorException(possibleEquation, possibleEquation, false);

        for(int i = 0; i < notUnarySuffixOperators.length; i++)
            if(possibleEquation.endsWith(notUnarySuffixOperators[i]))
                throw new TrailingOperatorException(possibleEquation, possibleEquation, true);

        if((possibleEquation.charAt(0) == '(')
        && (StringUtils.getMatchingBracketPosition(possibleEquation, 0) == possibleEquation.length() - 1))
        {
            return parse(possibleEquation.substring(1, possibleEquation.length() - 1));
        }

        int opPosition;

        if((opPosition = getOperatorPositionInString(possibleEquation, '+')) >= 0)
        {
            String l = possibleEquation.substring(0, opPosition);
            String r = possibleEquation.substring(opPosition + 1);
            return new Addition(parse(l), parse(r));
        }

        if((opPosition = getOperatorPositionInString(possibleEquation, '-', 1)) >= 0)
        {
            String l = possibleEquation.substring(0, opPosition);
            String r = possibleEquation.substring(opPosition + 1);
            return new Subtraction(parse(l), parse(r));
        }

        if((opPosition = getOperatorPositionInString(possibleEquation, '*')) >= 0)
        {
            String l = possibleEquation.substring(0, opPosition);
            String r = possibleEquation.substring(opPosition + 1);
            return new Multiplication(parse(l), parse(r));
        }

        if((opPosition = getOperatorPositionInString(possibleEquation, '×')) >= 0)
        {
            String l = possibleEquation.substring(0, opPosition);
            String r = possibleEquation.substring(opPosition + 1);
            return new Multiplication(parse(l), parse(r));
        }

        if((opPosition = getOperatorPositionInString(possibleEquation, '/')) >= 0)
        {
            String l = possibleEquation.substring(0, opPosition);
            String r = possibleEquation.substring(opPosition + 1);
            return new Division(parse(l), parse(r));
        }

        if((opPosition = getOperatorPositionInString(possibleEquation, '÷')) >= 0)
        {
            String l = possibleEquation.substring(0, opPosition);
            String r = possibleEquation.substring(opPosition + 1);
            return new Division(parse(l), parse(r));
        }

        if((opPosition = getOperatorPositionInString(possibleEquation, '^')) >= 0)
        {
            String l = possibleEquation.substring(0, opPosition);
            String r = possibleEquation.substring(opPosition + 1);
            return new Exponent(parse(l), parse(r));
        }

        if((opPosition = getOperatorPositionInString(possibleEquation, '√', 1)) >= 0)
        {
            String l = possibleEquation.substring(0, opPosition);
            String r = possibleEquation.substring(opPosition + 1);
            return new Root(parse(l), parse(r));
        }

        if(possibleEquation.startsWith("-"))
        {
            String o = possibleEquation.substring(1);
            return new Negation(parse(o));
        }

        if(possibleEquation.startsWith("√"))
        {
            String o = possibleEquation.substring(1);
            return new Root(new LiteralNumber(2), parse(o));
        }

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

    private static int getOperatorPositionInString(String s, char operator, int positionToStartLookingAt)
    {
        final int maxi = s.length() - 1;
        int bracketDepth = 0;

        for(int i = positionToStartLookingAt; i <= maxi; i++)
        {
            char ichar = s.charAt(i);

            if(ichar == '(')
                bracketDepth++;
            else if(ichar == ')')
            {
                if(bracketDepth > 0)
                    bracketDepth--;
            }
            else if(ichar == operator && bracketDepth == 0)
                return i;
        }

        return -1;
    }

    private static int getOperatorPositionInString(String s, char operator)
    { return getOperatorPositionInString(s, operator, 0); }

    private static int getOperatorPositionInString(String s, String operator, int positionToStartLookingAt)
    {
        final int maxi = s.length() - operator.length();
        final int opLength = operator.length();
        int bracketDepth = 0;

        if(opLength == 1)
            return getOperatorPositionInString(s, operator.charAt(0), positionToStartLookingAt);

        for(int i = positionToStartLookingAt; i <= maxi; i++)
        {
            char ichar = s.charAt(i);

            if(ichar == '(')
                bracketDepth++;
            else if(ichar == ')')
            {
                if(bracketDepth > 0)
                    bracketDepth--;
            }
            else if(bracketDepth == 0)
            {
                if(s.substring(i, i + opLength).equals(operator))
                    return i;
            }
        }

        return -1;
    }

    private static int getOperatorPositionInString(String s, String operator)
    { return getOperatorPositionInString(s, operator, 0); }

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
}
