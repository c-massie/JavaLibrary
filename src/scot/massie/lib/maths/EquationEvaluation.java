package scot.massie.lib.maths;

import scot.massie.lib.utils.StringUtils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
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

    }

    private static abstract class BinaryOperation extends Operation
    {
        public BinaryOperation(EquationComponent l, EquationComponent r)
        {
            leftOperand = l;
            rightOperand = r;
        }

        public EquationComponent leftOperand, rightOperand;
    }

    private static abstract class UnaryOperation extends Operation
    {
        public UnaryOperation(EquationComponent o)
        { operand = o; }

        public EquationComponent operand;
    }

    private static abstract class VariadicOperation extends Operation
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
        // example test function. TO DO: Replace with proper premade functions like sin/cos/tan
        functionMap.put("triple", value -> value[0] * 3);
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

    private static EquationComponent parse(Map<String, Double> variableValues,
                                           Map<String, ToDoubleFunction<double[]>> functionMap,
                                           String possibleEquation)
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
            return parse(variableValues, functionMap, possibleEquation.substring(1, possibleEquation.length() - 1));
        }

        int opPosition;

        if((opPosition = getOperatorPositionInString(possibleEquation, '+')) >= 0)
        {
            String l = possibleEquation.substring(0, opPosition);
            String r = possibleEquation.substring(opPosition + 1);
            return new Addition(parse(variableValues, functionMap, l), parse(variableValues, functionMap, r));
        }

        if((opPosition = getOperatorPositionInString(possibleEquation, '-', 1)) >= 0)
        {
            String l = possibleEquation.substring(0, opPosition);
            String r = possibleEquation.substring(opPosition + 1);
            return new Subtraction(parse(variableValues, functionMap, l), parse(variableValues, functionMap, r));
        }

        if((opPosition = getOperatorPositionInString(possibleEquation, '*')) >= 0)
        {
            String l = possibleEquation.substring(0, opPosition);
            String r = possibleEquation.substring(opPosition + 1);
            return new Multiplication(parse(variableValues, functionMap, l), parse(variableValues, functionMap, r));
        }

        if((opPosition = getOperatorPositionInString(possibleEquation, '×')) >= 0)
        {
            String l = possibleEquation.substring(0, opPosition);
            String r = possibleEquation.substring(opPosition + 1);
            return new Multiplication(parse(variableValues, functionMap, l), parse(variableValues, functionMap, r));
        }

        if((opPosition = getOperatorPositionInString(possibleEquation, '/')) >= 0)
        {
            String l = possibleEquation.substring(0, opPosition);
            String r = possibleEquation.substring(opPosition + 1);
            return new Division(parse(variableValues, functionMap, l), parse(variableValues, functionMap, r));
        }

        if((opPosition = getOperatorPositionInString(possibleEquation, '÷')) >= 0)
        {
            String l = possibleEquation.substring(0, opPosition);
            String r = possibleEquation.substring(opPosition + 1);
            return new Division(parse(variableValues, functionMap, l), parse(variableValues, functionMap, r));
        }

        if((opPosition = getOperatorPositionInString(possibleEquation, '^')) >= 0)
        {
            String l = possibleEquation.substring(0, opPosition);
            String r = possibleEquation.substring(opPosition + 1);
            return new Exponent(parse(variableValues, functionMap, l), parse(variableValues, functionMap, r));
        }

        if((opPosition = getOperatorPositionInString(possibleEquation, '√', 1)) >= 0)
        {
            String l = possibleEquation.substring(0, opPosition);
            String r = possibleEquation.substring(opPosition + 1);
            return new Root(parse(variableValues, functionMap, l), parse(variableValues, functionMap, r));
        }

        if(possibleEquation.startsWith("-"))
        {
            String o = possibleEquation.substring(1);
            return new Negation(parse(variableValues, functionMap, o));
        }

        if(possibleEquation.startsWith("√"))
        {
            String o = possibleEquation.substring(1);
            return new Root(new LiteralNumber(2), parse(variableValues, functionMap, o));
        }

        if(variableValues.containsKey(possibleEquation))
            return new EquationVariable(variableValues, possibleEquation);

        FunctionNameAndArgumentString fAndA = getFunctionNameAndArgumentString(possibleEquation);

        parseFunctionCall: // TO DO: Split off into own function
        if((fAndA != null) && (functionMap.containsKey(fAndA.getFunctionName())))
        {
            String argsString = fAndA.getArgumentString();
            int argsStringLength = argsString.length();
            int bracketDepth = 0;
            int lastSplit = -1;
            List<String> arguments = new ArrayList<>();

            if(argsString.startsWith(",") || argsString.endsWith(","))
                break parseFunctionCall;

            if(!argsString.isEmpty())
            {
                for(int i = 0; i < argsStringLength; i++)
                {
                    char ichar = argsString.charAt(i);

                    if(ichar == '(')
                        bracketDepth++;
                    else if(ichar == ')')
                    {
                        if(--bracketDepth < 0)
                            break parseFunctionCall;
                    }
                    else if(ichar == ',')
                    {
                        arguments.add(argsString.substring(lastSplit + 1, i).trim());
                        lastSplit = i;
                    }
                }

                arguments.add(argsString.substring(lastSplit + 1));
            }

            EquationComponent[] parsedArguments = new EquationComponent[arguments.size()];

            for(int i = 0; i < parsedArguments.length; i++)
                parsedArguments[i] = parse(variableValues, functionMap, arguments.get(i));

            return new FunctionCall(functionMap, fAndA.getFunctionName(), parsedArguments);
        }

        try
        {
            double literalNumberValue = Double.parseDouble(possibleEquation);
            return new LiteralNumber(literalNumberValue);
        }
        catch(NumberFormatException e)
        { }

        throw new UnparsableEquationException(possibleEquation, possibleEquation);
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

    public EquationEvaluation build()
    {
        try
        { topLevelComponent = parse(variableValues, functionMap, equation); }
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
