package scot.massie.lib.maths;

import scot.massie.lib.utils.StringUtils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

public final class EquationEvaluation
{
    public static class UnparsableEquationException extends RuntimeException
    {
        public UnparsableEquationException(String fullEquation, String equationSection)
        {
            super("Equation was not parsable as an equation: " + fullEquation +
                  "\nSpecifically, this portion: " + equationSection);
        }

        String equationSection;
        String fullEquation;

        public String getFullEquation()
        { return fullEquation; }

        public String getEquationSection()
        { return equationSection; }
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

    private static EquationComponent parse(Map<String, Double> variableValues, String possibleEquation)
    {
        possibleEquation = possibleEquation.trim();

        if((possibleEquation.charAt(0) == '(')
        && (StringUtils.getMatchingBracketPosition(possibleEquation, 0) == possibleEquation.length() - 1))
        {
            return parse(variableValues, possibleEquation.substring(1, possibleEquation.length() - 1));
        }

        int opPosition;

        if((opPosition = getOperatorPositionInString(possibleEquation, '+')) >= 0)
        {
            String l = possibleEquation.substring(0, opPosition);
            String r = possibleEquation.substring(opPosition + 1);
            return new Addition(parse(variableValues, l), parse(variableValues, r));
        }

        if((opPosition = getOperatorPositionInString(possibleEquation, '-', 1)) >= 0)
        {
            String l = possibleEquation.substring(0, opPosition);
            String r = possibleEquation.substring(opPosition + 1);
            return new Subtraction(parse(variableValues, l), parse(variableValues, r));
        }

        if((opPosition = getOperatorPositionInString(possibleEquation, '*')) >= 0)
        {
            String l = possibleEquation.substring(0, opPosition);
            String r = possibleEquation.substring(opPosition + 1);
            return new Multiplication(parse(variableValues, l), parse(variableValues, r));
        }

        if((opPosition = getOperatorPositionInString(possibleEquation, '×')) >= 0)
        {
            String l = possibleEquation.substring(0, opPosition);
            String r = possibleEquation.substring(opPosition + 1);
            return new Multiplication(parse(variableValues, l), parse(variableValues, r));
        }

        if((opPosition = getOperatorPositionInString(possibleEquation, '/')) >= 0)
        {
            String l = possibleEquation.substring(0, opPosition);
            String r = possibleEquation.substring(opPosition + 1);
            return new Division(parse(variableValues, l), parse(variableValues, r));
        }

        if((opPosition = getOperatorPositionInString(possibleEquation, '÷')) >= 0)
        {
            String l = possibleEquation.substring(0, opPosition);
            String r = possibleEquation.substring(opPosition + 1);
            return new Division(parse(variableValues, l), parse(variableValues, r));
        }

        if((opPosition = getOperatorPositionInString(possibleEquation, '^')) >= 0)
        {
            String l = possibleEquation.substring(0, opPosition);
            String r = possibleEquation.substring(opPosition + 1);
            return new Exponent(parse(variableValues, l), parse(variableValues, r));
        }

        if((opPosition = getOperatorPositionInString(possibleEquation, '√', 1)) >= 0)
        {
            String l = possibleEquation.substring(0, opPosition);
            String r = possibleEquation.substring(opPosition + 1);
            return new Root(parse(variableValues, l), parse(variableValues, r));
        }

        if(possibleEquation.startsWith("-"))
        {
            String o = possibleEquation.substring(1);
            return new Negation(parse(variableValues, o));
        }

        if(possibleEquation.startsWith("√"))
        {
            String o = possibleEquation.substring(1);
            return new Root(new LiteralNumber(2), parse(variableValues, o));
        }

        if(variableValues.containsKey(possibleEquation))
            return new EquationVariable(variableValues, possibleEquation);

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

    public EquationEvaluation build()
    {
        topLevelComponent = parse(variableValues, equation);
        return this;
    }

    public double evaluate()
    {
        if(topLevelComponent == null)
            build();

        return topLevelComponent.evaluate();
    }

    public EquationEvaluation withArgument(String argumentRepresentedBy, double argumentValue)
    {
        variableValues.put(argumentRepresentedBy, argumentValue);
        return this;
    }
}
