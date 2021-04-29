package scot.massie.lib.maths;

import org.assertj.core.util.Lists;
import scot.massie.lib.utils.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.ToDoubleFunction;

/**
 * <p>A representation of an evaluation of an equation.</p>
 *
 * <p>Is finalised with .build(). Before being finalised, can have custom functions, variables, and operators defined.
 * Once finalised, can no longer have custom operators defined as this would require the equation to be reëvaluated, but
 * can still have variables and functions re-assigned.</p>
 *
 * <p>The default operators have the following precedence levels:</p>
 *
 * <ul>
 *     <li>Custom binary operators by default: 0</li>
 *     <li>Addition/subtraction: 100</li>
 *     <li>Multiplication/division: 200</li>
 *     <li>Modulo: 300</li>
 *     <li>Custom unary operators by default: 400</li>
 *     <li>Unary positive/negative: 500</li>
 *     <li>nth root: 600</li>
 *     <li>Square root unary: 700</li>
 *     <li>Exponentiation: 800</li>
 *     <li>Percentage: 900</li>
 * </ul>
 */
public final class EquationEvaluation
{
    /*

    TO DO: Add support for operators of arbitrary length rather than one character. Operators should be tokenised in
           order of registration, with a note to register operators that may be mistake as smaller operators sharing the
           same characters first.

           This may require the default operators, which are all single-character, to be registered after any custom
           operators of more characters. Alternatively, they could be added with a tokenisation precedence level
           making them be tokenised after custom operators. This may be the way to go if I add default multi-character
           operators.


    NEXT TO DO:
        - Implement parsing support for InfixOperator.
        - - Includes recognising tokens used in N-ary infix operators.
        - Implement ability to register arbitrary N N-ary operators on the public interface.
        - Replace all binary operators with infix operators.
        - Remove binary operator code.
     */

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

    /**
     * Thrown to indicate that the contained equation cannot be parsed given the operators, functions, and variables
     * available to it.
     */
    public static class UnparsableEquationException extends RuntimeException
    {
        /**
         * Creates a new UnparsableEquationException.
         * @param fullEquation The full equation that could not be parsed.
         * @param equationSection The specific section of the equation that could not be parsed.
         */
        public UnparsableEquationException(String fullEquation, String equationSection)
        {
            super("Equation was not parsable as an equation: " + fullEquation +
                  "\nSpecifically, this portion: " + equationSection);

            this.fullEquation = fullEquation;
            this.equationSection = equationSection;
        }

        /**
         * Creates a new UnparsableEquationException with a custom message.
         * @param fullEquation The full equation that could not be parsed.
         * @param equationSection The specific section of the equation that could not be parsed.
         * @param msg The exception message.
         */
        UnparsableEquationException(String fullEquation, String equationSection, String msg)
        {
            super(msg);
            this.fullEquation = fullEquation;
            this.equationSection = equationSection;
        }

        final String equationSection;
        final String fullEquation;

        /**
         * Gets the full equation that could not be parsed.
         * @return The full equation that could not be parsed.
         */
        public String getFullEquation()
        { return fullEquation; }

        /**
         * Gets the specific section of the equation that could not be parsed.
         * @return The specific section of the equation that could not be parsed.
         */
        public String getEquationSection()
        { return equationSection; }

        /**
         * Creates a copy of this exception replacing the full equation stored with the given one.
         * @param fullEquation The full equation that could not be parsed.
         * @return The copy created.
         */
        public UnparsableEquationException withFullEquation(String fullEquation)
        { return new UnparsableEquationException(fullEquation, equationSection); }
    }

    /**
     * Thrown to indicate that the contained equation could not be parsed, as it started or ended with a binary operator
     * that could not be a prefix or postfix respectively.
     */
    public static class TrailingOperatorException extends UnparsableEquationException
    {
        /**
         * Creates a new TrailingOperatorException.
         * @param fullEquation The full equation that could not be parsed.
         * @param equationSection The specific section of the equation that could not be parsed.
         * @param operatorIsAtEnd Whether or not the dangling binary operator was at the end. If not, then it's assumed
         *                        to be at the start.
         */
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

        /**
         * Gets whether or not the dangling binary operator is at the end of the equation.
         * @return True if the dangling binary operator is at the end of the equation. Otherwise, false.
         */
        public boolean operatorIsAtEnd()
        { return operatorIsAtEnd; }

        /**
         * Gets whether or not the dangling binary operator is at the start of the equation.
         * @return True if the dangling binary operator is at the start of the equation. Otherwise, false.
         */
        public boolean operatorIsAtStart()
        { return !operatorIsAtEnd; }

        /**
         * Creates a copy of this exception replacing the full equation stored with the given one.
         * @param fullEquation The full equation that could not be parsed.
         * @return The copy created.
         */
        @Override
        public TrailingOperatorException withFullEquation(String fullEquation)
        { return new TrailingOperatorException(fullEquation, equationSection, operatorIsAtEnd); }
    }

    /**
     * Thrown when a parsed variable in an equation is not present in the variables assigned to the equation.
     */
    public static final class MissingVariableException extends RuntimeException
    {
        /**
         * Creates a new UnresolvedArgumentInEquationException.
         * @param variableName The name of the variable not found.
         */
        public MissingVariableException(String variableName)
        {
            super("Variable was unresolved: " + variableName);
            this.variableName = variableName;
        }

        private final String variableName;

        /**
         * Gets the name of the variable not found.
         * @return The name of the variable not found.
         */
        public String getVariableName()
        { return variableName; }
    }

    /**
     * Thrown when a parsed function in an equation is not present in the functions assigned to the equation.
     */
    public static final class MissingFunctionException extends RuntimeException
    {
        /**
         * Creates a new MissingFunctionException
         * @param functionName The name of the function not found.
         */
        public MissingFunctionException(String functionName)
        {
            super("Function was missing: " + functionName);
            this.functionName = functionName;
        }

        private final String functionName;

        /**
         * Gets the name of the function not found.
         * @return The name of the function not found.
         */
        public String getFunctionName()
        { return functionName; }
    }

    /**
     * Thrown when a function requires more arguments than were assigned to it.
     */
    public static final class MissingFunctionArgumentsException extends RuntimeException
    {
        /**
         * Creates a new MissingFunctionArgumentsException.
         * @param functionName The name of the function missing arguments.
         * @param numberOfArgsRequired The number of arguments provided.
         * @param numberOfArgsProvided The number of arguments required.
         */
        public MissingFunctionArgumentsException(String functionName,
                                                 int numberOfArgsRequired,
                                                 int numberOfArgsProvided)
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

        /**
         * Gets the name of the function requiring more arguments.
         * @return The name of the function.
         */
        public String getFunctionName()
        { return functionName; }

        /**
         * Gets the number of arguments required by the function.
         * @return The number of arguments required.
         */
        public int getNumberOfArgsRequired()
        { return numberOfArgsRequired; }

        /**
         * Gets the number of arguments passed into the function.
         * @return The number of arguments passed.
         */
        public int getNumberOfArgsProvided()
        { return numberOfArgsProvided; }
    }
    //endregion

    //region Operator actions

    /**
     * An action performable in an equation by a unary operator.
     */
    @FunctionalInterface
    public interface UnaryOperatorAction
    {
        /**
         * Performs an operation on the passed operand.
         * @param x The operand.
         * @return The result of the operation.
         */
        double performOperation(double x);
    }

    /**
     * An action performable in an equation by a binary operator.
     */
    @FunctionalInterface
    public interface BinaryOperatorAction
    {
        /**
         * Performs an operation on the passed operands.
         * @param l The left operand.
         * @param r The right operand.
         * @return The result of the operation.
         */
        double performOperation(double l, double r);
    }

    @FunctionalInterface
    public interface InfixOperatorAction
    {
        /**
         * Performs an operation on the passed operands.
         * @param operands An array of operands being passed into this operator.
         * @return The result of the operation.
         */
        double performOperation(double... operands);
    }
    //endregion

    //region misc
    private static class OperatorGroup
    {
        public OperatorGroup(double priority)
        { this.priority = priority; }

        double priority;

        List<InfixOperator> leftAssociativeInfixOperators = new ArrayList<>();
        List<InfixOperator> rightAssociativeInfixOperators = new ArrayList<>();
        List<BinaryOperator> leftAssociativeBinaryOperators = new ArrayList<>();
        List<BinaryOperator> rightAssociativeBinaryOperators = new ArrayList<>();
        List<UnaryOperator> prefixOperators = new ArrayList<>();
        List<UnaryOperator> postfixOperators = new ArrayList<>();

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
            else if(op instanceof InfixOperator)
            {
                InfixOperator iop = (InfixOperator)op;

                if(iop.isLeftAssociative)
                    leftAssociativeInfixOperators.add(iop);
                else
                    rightAssociativeInfixOperators.add(iop);
            }
            else // op is UnaryOperator
            {
                UnaryOperator uop = (UnaryOperator)op;

                if(uop.isPostfixOperator)
                    postfixOperators.add(uop);
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
        public UnaryOperator(char lex, double priority, UnaryOperatorAction action, boolean isPostfixOperator)
        {
            super(lex, priority);
            this.action = action;
            this.isPostfixOperator = isPostfixOperator;
        }

        public UnaryOperator(char lex, double priority, UnaryOperatorAction action)
        { this(lex, priority, action, false); }

        UnaryOperatorAction action;
        boolean isPostfixOperator;
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

    private static class InfixOperator extends Operator
    {
        public InfixOperator(char lex, double priority, InfixOperatorAction action)
        { this(new char[]{ lex }, priority, action); }

        public InfixOperator(char lex1, char lex2, double priority, InfixOperatorAction action)
        { this(new char[]{ lex1, lex2 }, priority, action); }

        public InfixOperator(char[] lex, double priority, InfixOperatorAction action)
        { this(lex, priority, action, true); }

        public InfixOperator(char lex, double priority, InfixOperatorAction action, boolean isLeftAssociative)
        { this(new char[]{ lex }, priority, action); }

        public InfixOperator(char lex1, char lex2, double priority, InfixOperatorAction action, boolean isLeftAssociative)
        { this(new char[]{ lex1, lex2 }, priority, action); }

        public InfixOperator(char[] lex, double priority, InfixOperatorAction action, boolean isLeftAssociative)
        {
            super(lex[0], priority);
            this.lexes = lex;
            this.action = action;
            this.isLeftAssociative = isLeftAssociative;
        }

        char[] lexes;
        InfixOperatorAction action;
        boolean isLeftAssociative;

        public InfixOperation tryParse(EquationEvaluation ee, String s)
        {
            String[] operands = new String[lexes.length + 1];
            EquationComponent[] components = new EquationComponent[operands.length];

            if(isLeftAssociative)
            {
                int nextLexPosition = -1;

                for(int i = lexes.length - 1; i >= 0; i--)
                {
                    int previousLexPosition = getPreviousLexPosition_leftAssociative(ee, lexes, s, lexes[i], nextLexPosition);

                    if(previousLexPosition < 0)
                        return null;

                    operands[i] = s.substring(previousLexPosition + 1, nextLexPosition);
                    nextLexPosition = previousLexPosition;
                }
            }
            else
            {
                int previousLexPosition = -1;

                for(int i = 0; i < lexes.length; i++)
                {
                    int nextLexPosition = getNextLexPosition_rightAssociative(ee, lexes, s, lexes[i], previousLexPosition);

                    if(nextLexPosition < 0)
                        return null;

                    operands[i] = s.substring(previousLexPosition + 1, nextLexPosition);
                    previousLexPosition = nextLexPosition;
                }
            }

            for(int i = 0; i < operands.length; i++)
                components[i] = ee.parse(operands[i]);

            return new InfixOperation(components, action);
        }

        private static int getNextLexPosition_rightAssociative
                (EquationEvaluation ee, char[] lexes, String s, char nextLex, int startFrom)
        {
            int slength = s.length();
            int bracketDepth = 0;
            char firstLex = lexes[0];

            for(int i = startFrom; i < slength; i++)
            {
                char c = s.charAt(i);

                if(c == '(')
                    bracketDepth++;
                else if(c == ')')
                {
                    if(bracketDepth-- < 0)
                        return -1;
                }
                else if(bracketDepth == 0)
                {
                    if(i == slength - 1)
                        continue;

                    if(!ee.canBeBinaryOperator(s, i))
                        continue;

                    if(c == nextLex)
                        return i;

                    if(c == firstLex)
                    {
                        int matchingLastLexPosition = getMatchingLastLexPosition_rightAssociative(ee, lexes, s, i);

                        if(matchingLastLexPosition >= 0)
                            i = matchingLastLexPosition;
                    }
                }
            }

            return -1;
        }

        private static int getMatchingLastLexPosition_rightAssociative
                (EquationEvaluation ee, char[] lexes, String s, int firstLexPosition)
        {
            int position = firstLexPosition;

            for(int i = 1; i < lexes.length; i++)
            {
                position = getNextLexPosition_rightAssociative(ee, lexes, s, lexes[i], position + 1);

                if(position < 0)
                    return -1;
            }

            return position;
        }

        private static int getPreviousLexPosition_leftAssociative(EquationEvaluation ee, char[] lexes, String s, char previousLex, int startFrom)
        {
            int slength = s.length();
            int bracketDepth = 0;
            char lastLex = lexes[lexes.length - 1];

            for(int i = startFrom; i >= 0; i--)
            {
                char c = s.charAt(i);

                if(c == ')')
                    bracketDepth++;
                else if(c == '(')
                {
                    if(bracketDepth-- < 0)
                        return -1;
                }
                else if(bracketDepth == 0)
                {
                    if(i == 0)
                        continue;

                    if(!ee.canBeBinaryOperator(s, i))
                        continue;

                    if(c == previousLex)
                        return i;

                    if(c == lastLex)
                    {
                        int matchingFirstLexPosition = getMatchingFirstLexPosition_leftAssociative(ee, lexes, s, i);

                        if(matchingFirstLexPosition >= 0)
                            i = matchingFirstLexPosition;
                    }
                }
            }

            return -1;
        }

        private static int getMatchingFirstLexPosition_leftAssociative
                (EquationEvaluation ee, char[] lexes, String s, int lastLexPosition)
        {
            int position = lastLexPosition;

            for(int i = lexes.length - 2; i >= 0; i--)
            {
                position = getPreviousLexPosition_leftAssociative(ee, lexes, s, lexes[i], position - 1);

                if(position < 0)
                    return -1;
            }

            return position;
        }
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
                throw new MissingVariableException(name);

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

    private static class InfixOperation extends Operation
    {
        public InfixOperation(EquationComponent[] operands, InfixOperatorAction action)
        {
            this.operands = operands;
            this.action = action;
        }

        EquationComponent[] operands;
        InfixOperatorAction action;

        @Override
        public double evaluate()
        {
            double[] evaluatedOps = new double[operands.length];

            for(int i = evaluatedOps.length - 1; i >= 0; i--)
                evaluatedOps[i] = operands[i].evaluate();

            return action.performOperation(evaluatedOps);
        }
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

    /**
     * Creates a new evaluation of the passed in equation. The result of the equation may be accessed by calling
     * .evaluate() on the EquationEvaluation instance.
     * @param equation The equation to evaluate.
     */
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
            new InfixOperator('?', ':', -100, o -> o[0] >= 1 ? o[1] : o[2]),
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

    private final Map<List<Character>, InfixOperator> infixOperators = new HashMap<>();

    private final Map<Character, UnaryOperator> prefixOperators = new HashMap<>();

    private final Map<Character, UnaryOperator> postfixOperators = new HashMap<>();

    private final Set<Character> operatorChars = new HashSet<>();
    //endregion

    //region Methods
    //region Convenience methods
    private static List<Character> charArrayToList(char[] array)
    {
        List<Character> result = new ArrayList<>();

        for(char i : array)
            result.add(i);

        return result;
    }
    //endregion

    //region Methods about state
    private boolean charIsNonPrefixOperator(char c)
    {
        for(char oc : operatorChars)
            if(c == oc)
                return !prefixOperators.containsKey(c);

        return false;
    }

    private boolean charIsNonPostfixOperator(char c)
    {
        for(char oc : operatorChars)
            if(c == oc)
                return !postfixOperators.containsKey(c);

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

    private void removeInfixOpFromOpGroupsIfRegistered(InfixOperator op)
    {
        List<Character> lexes = charArrayToList(op.lexes);
        InfixOperator existingOp = infixOperators.get(lexes);

        if(existingOp != null)
        {
            for(OperatorGroup og : operatorGroups)
            {
                if(og.leftAssociativeInfixOperators.remove(existingOp))
                    break;

                if(og.rightAssociativeInfixOperators.remove(existingOp))
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

    private void removePostfixOpFromOpGroupsIfRegistered(UnaryOperator op)
    {
        UnaryOperator existingOp = prefixOperators.get(op.lex);

        if(existingOp != null)
            for(OperatorGroup og : operatorGroups)
                if(og.postfixOperators.remove(existingOp))
                    break;
    }

    private void addOperator(Operator op)
    {
        if(op instanceof BinaryOperator)
        {
            removeBinaryOpFromOpGroupsIfRegistered((BinaryOperator)op);
            binaryOperators.put(op.lex, (BinaryOperator)op);
        }
        else if(op instanceof InfixOperator)
        {
            removeInfixOpFromOpGroupsIfRegistered((InfixOperator)op);
            infixOperators.put(charArrayToList(((InfixOperator)op).lexes), (InfixOperator)op);
        }
        else
        {
            UnaryOperator uop = (UnaryOperator)op;

            if(uop.isPostfixOperator)
            {
                removePostfixOpFromOpGroupsIfRegistered(uop);
                postfixOperators.put(uop.lex, uop);
            }
            else
            {
                removePrefixOpFromOpGroupsIfRegistered(uop);
                prefixOperators.put(uop.lex, uop);
            }
        }

        getOrCreateOpGroup(op.priority).addOperator(op);

        if(op instanceof InfixOperator)
        {
            for(char i : ((InfixOperator)op).lexes)
                operatorChars.add(i);
        }
        else
            operatorChars.add(op.lex);
    }

    private void addOperatorDumbly(Operator op)
    {
        if(op instanceof BinaryOperator)
            binaryOperators.put(op.lex, (BinaryOperator)op);
        else if(op instanceof InfixOperator)
            infixOperators.put(charArrayToList(((InfixOperator)op).lexes), (InfixOperator)op);
        else
        {
            UnaryOperator uop = (UnaryOperator)op;
            (uop.isPostfixOperator ? postfixOperators : prefixOperators).put(uop.lex, uop);
        }

        getOrCreateOpGroup(op.priority).addOperator(op);

        if(op instanceof InfixOperator)
        {
            for(char i : ((InfixOperator)op).lexes)
                operatorChars.add(i);
        }
        else
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

        if(charIsNonPostfixOperator(possibleEquation.charAt(possibleEquation.length() - 1)))
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
        Operation o;

        if(!og.rightAssociativeBinaryOperators.isEmpty())
        {
            o = parseOperation_rightAssociative(s, og);

            if(o != null)
                return o;
        }

        if(!og.leftAssociativeBinaryOperators.isEmpty())
        {
            o = parseOperation_leftAssociative(s, og);

            if(o != null)
                return o;
        }

        o = parseOperation_infix(s, og.rightAssociativeInfixOperators);

        if(o != null)
            return o;

        o = parseOperation_infix(s, og.rightAssociativeInfixOperators);

        if(o != null)
            return o;

        if(!og.postfixOperators.isEmpty())
        {
            o = parseOperation_postfix(s, og);

            if(o != null)
                return o;
        }

        if(!og.prefixOperators.isEmpty())
        {
            o = parseOperation_prefix(s, og);

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

    private Operation parseOperation_infix(String s, Collection<InfixOperator> operators)
    {
        for(InfixOperator op : operators)
        {
            InfixOperation operation = op.tryParse(this, s);

            if(operation != null)
                return operation;
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

    private Operation parseOperation_postfix(String s, OperatorGroup og)
    {
        int lastPos = s.length() - 1;

        for(UnaryOperator uop : og.postfixOperators)
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

            if((ichar != ' ') && (!postfixOperators.containsKey(ichar)))
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

    /**
     * Finalises the equation evaluation. After being finalised, the equation can be evaluated without having re-parse
     * it. Once finalised, new operators may not be registered, but operators and functions may still be reassigned.
     * @return This equation evaluation.
     */
    public EquationEvaluation build()
    {
        try
        { topLevelComponent = parse(equation); }
        catch(UnparsableEquationException ex)
        { throw ex.withFullEquation(equation); }
        return this;
    }

    /**
     * Gets the result of the contained equation. If the equation evaluation isn't finalised, finalises it first.
     * @return The result of the contained equation.
     */
    public double evaluate()
    {
        if(topLevelComponent == null)
            build();

        return topLevelComponent.evaluate();
    }

    /**
     * Gets the result of the contained equation, rounded to the nearest int. If the equation evaluation isn't
     * finalised, finalises it first.
     * @return The result of the contained equation as an int.
     */
    public int evaluateToInt()
    {
        long result = evaluateToLong();

        if(result > (long)Integer.MAX_VALUE)
            return Integer.MAX_VALUE;

        if(result < (long)Integer.MIN_VALUE)
            return Integer.MIN_VALUE;

        return (int)result;
    }

    /**
     * Gets the result of the contained equation, rounded to the nearest 64-bit integer (long). If the equation
     * evaluation isn't finalised, finalises it first. Is the equivalent to Math.round(evaluate()).
     * @return The result of the contained equation as a long.
     */
    public long evaluateToLong()
    { return Math.round(evaluate()); }

    /**
     * Registers a new variable available to the contained equation. Variables may be any string not containing
     * operators.
     * @param argumentRepresentedBy How the argument may be referred to in the equation.
     * @param argumentValue The value of the argument.
     * @return This equation evaluation.
     */
    public EquationEvaluation withVariable(String argumentRepresentedBy, double argumentValue)
    {
        variableValues.put(argumentRepresentedBy, argumentValue);
        return this;
    }

    /**
     * Registers a new function available to the contained equation. Functions may be called by having the name of the
     * function followed by the arguments to pass into the function in brackets, or an empty pair of brackets where
     * no arguments are to be passed in. Function names may be any string not containing operators.
     * @param functionName The name of the function, by which the function may be referred to in the equation.
     * @param f The function to register. The arguments are passed in as an array of doubles.
     * @return This equation evaluation.
     */
    public EquationEvaluation withFunction(String functionName, ToDoubleFunction<double[]> f)
    {
        functionMap.put(functionName, f);
        return this;
    }

    /**
     * Registers a new binary operator. Operators being registered will not be factored into the equation until the next
     * time it's built, (and not simply evaluated) as the introduction of a new operator may change the parsing of the
     * equation. Operators are left-associative by default.
     * @param operatorCharacter The character by which the operator may be referred to in the equation.
     * @param precedenceLevel The precedence level of the operator. Higher levels are more "sticky".
     * @param calculation The calculation of a result of the two operands.
     * @return This equation evaluation.
     */
    public EquationEvaluation withOperator(char operatorCharacter,
                                           double precedenceLevel,
                                           BinaryOperatorAction calculation)
    {
        addOperator(new BinaryOperator(operatorCharacter, precedenceLevel, calculation));
        return this;
    }

    /**
     * <p>Registers a new binary operator. Operators being registered will not be factored into the equation until the
     * next time it's built, (and not simply evaluated) as the introduction of a new operator may change the parsing of
     * the equation. Operators are left-associative by default.</p>
     *
     * <p>Binary operators have a default precedence level of 0.</p>
     * @param operatorCharacter The character by which the operator may be referred to in the equation.
     * @param calculation The calculation of a result of the two operands.
     * @return This equation evaluation.
     */
    public EquationEvaluation withOperator(char operatorCharacter,
                                           BinaryOperatorAction calculation)
    { return withOperator(operatorCharacter, 0, calculation); }

    /**
     * Registers a new right-associative binary operator. Operators being registered will not be factored into the
     * equation until the next time it's built, (and not simply evaluated) as the introduction of a new operator may
     * change the parsing of the equation.
     * @param operatorCharacter The character by which the operator may be referred to in the equation.
     * @param precedenceLevel The precedence level of the operator. Higher levels are more "sticky".
     * @param calculation The calculation of a result of the two operands.
     * @return This equation evaluation.
     */
    public EquationEvaluation withRightAssociativeOperator(char operatorCharacter,
                                                           double precedenceLevel,
                                                           BinaryOperatorAction calculation)
    {
        addOperator(new BinaryOperator(operatorCharacter, precedenceLevel, calculation, false));
        return this;
    }

    /**
     * <p>Registers a new right-associative binary operator. Operators being registered will not be factored into the
     * equation until the next time it's built, (and not simply evaluated) as the introduction of a new operator may
     * change the parsing of the equation.</p>
     *
     * <p>Binary operators have a default precedence level of 0.</p>
     * @param operatorCharacter The character by which the operator may be referred to in the equation.
     * @param calculation The calculation of a result of the two operands.
     * @return This equation evaluation.
     */
    public EquationEvaluation withRightAssociativeOperator(char operatorCharacter,
                                                           BinaryOperatorAction calculation)
    { return withRightAssociativeOperator(operatorCharacter, 0, calculation); }

    /**
     * Registers a new prefix operator. Operators being registered will not be factored into the equation until the next
     * time it's built, (and not simply evaluated) as the introduction of a new operator may change the parsing of the
     * equation.
     * @param operatorCharacter The character by which the operator may be referred to in the equation.
     * @param precedenceLevel The precedence level of the operator. Higher levels are more "sticky".
     * @param calculation The calculation of a result of the operand.
     * @return This equation evaluation.
     */
    public EquationEvaluation withPrefixOperator(char operatorCharacter,
                                                 double precedenceLevel,
                                                 UnaryOperatorAction calculation)
    {
        addOperator(new UnaryOperator(operatorCharacter, precedenceLevel, calculation, false));
        return this;
    }

    /**
     * <p>Registers a new prefix operator. Operators being registered will not be factored into the equation until the
     * next time it's built, (and not simply evaluated) as the introduction of a new operator may change the parsing of
     * the equation.</p>
     *
     * <p>Unary operators have a default precedence level of 0.</p>
     * @param operatorCharacter The character by which the operator may be referred to in the equation.
     * @param calculation The calculation of a result of the operand.
     * @return This equation evaluation.
     */
    public EquationEvaluation withPrefixOperator(char operatorCharacter,
                                                 UnaryOperatorAction calculation)
    {
        addOperator(new UnaryOperator(operatorCharacter, 4, calculation, false));
        return this;
    }

    /**
     * Registers a new postfix operator. Operators being registered will not be factored into the equation until the
     * next time it's built, (and not simply evaluated) as the introduction of a new operator may change the parsing of
     * the equation.
     * @param operatorCharacter The character by which the operator may be referred to in the equation.
     * @param precedenceLevel The precedence level of the operator. Higher levels are more "sticky".
     * @param calculation The calculation of a result of the operand.
     * @return This equation evaluation.
     */
    public EquationEvaluation withPostfixOperator(char operatorCharacter,
                                                 double precedenceLevel,
                                                 UnaryOperatorAction calculation)
    {
        addOperator(new UnaryOperator(operatorCharacter, precedenceLevel, calculation, true));
        return this;
    }

    /**
     * <p>Registers a new postfix operator. Operators being registered will not be factored into the equation until the
     * next time it's built, (and not simply evaluated) as the introduction of a new operator may change the parsing of
     * the equation.</p>
     *
     * <p>Unary operators have a default precedence level of 0.</p>
     * @param operatorCharacter The character by which the operator may be referred to in the equation.
     * @param calculation The calculation of a result of the operand.
     * @return This equation evaluation.
     */
    public EquationEvaluation withPostfixOperator(char operatorCharacter,
                                                 UnaryOperatorAction calculation)
    {
        addOperator(new UnaryOperator(operatorCharacter, 4, calculation, true));
        return this;
    }

    /**
     * Stops a variable from being usable in the equation.
     * @param variable The variable to remove.
     * @return This equation evaluation.
     */
    public EquationEvaluation withoutVariable(String variable)
    {
        variableValues.remove(variable);
        return this;
    }

    /**
     * Stops a function from being usable in the equation.
     * @param functionName The name of the function to remove.
     * @return This equation evaluation.
     */
    public EquationEvaluation withoutFunction(String functionName)
    {
        functionMap.remove(functionName);
        return this;
    }

    /**
     * Stops a binary operator from being usable in the equation.
     * @param operator The operator to remove.
     * @return This equation evaluation.
     */
    public EquationEvaluation withoutBinaryOperator(char operator)
    {
        BinaryOperator bop = binaryOperators.remove(operator);

        if(bop != null)
        {
            boolean opCharStillUsed = false;

            for(OperatorGroup og : operatorGroups)
            {
                if(og.leftAssociativeBinaryOperators.remove(bop))
                    break;

                if(og.rightAssociativeBinaryOperators.remove(bop))
                    break;
            }

            ogloop:
            for(OperatorGroup og : operatorGroups)
            {
                for(UnaryOperator uop : og.prefixOperators)
                   if(uop.lex == operator)
                   {
                       opCharStillUsed = true;
                       break ogloop;
                   }

                for(UnaryOperator uop : og.postfixOperators)
                    if(uop.lex == operator)
                    {
                        opCharStillUsed = true;
                        break ogloop;
                    }
            }

            if(!opCharStillUsed)
                operatorChars.remove(operator);
        }

        return this;
    }

    /**
     * Stops a prefix operator from being usable in the equation.
     * @param operator The operator to remove.
     * @return This equation evaluation.
     */
    public EquationEvaluation withoutPrefixOperator(char operator)
    {
        UnaryOperator uop = prefixOperators.remove(operator);

        if(uop != null)
        {
            boolean opCharStillUsed = false;

            for(OperatorGroup og : operatorGroups)
                if(og.prefixOperators.remove(uop))
                    break;

            ogloop:
            for(OperatorGroup og: operatorGroups)
            {
                for(UnaryOperator sop : og.postfixOperators)
                    if(sop.lex == operator)
                    {
                        opCharStillUsed = true;
                        break ogloop;
                    }

                for(BinaryOperator bop : og.leftAssociativeBinaryOperators)
                    if(bop.lex == operator)
                    {
                        opCharStillUsed = true;
                        break ogloop;
                    }

                for(BinaryOperator bop : og.rightAssociativeBinaryOperators)
                    if(bop.lex == operator)
                    {
                        opCharStillUsed = true;
                        break ogloop;
                    }
            }

            if(!opCharStillUsed)
                operatorChars.remove(operator);
        }

        return this;
    }

    /**
     * Stops a postfix operator from being usable in the equation.
     * @param operator The operator to remove.
     * @return This equation evaluation.
     */
    public EquationEvaluation withoutPostfixOperator(char operator)
    {
        UnaryOperator uop = postfixOperators.remove(operator);

        if(uop != null)
        {
            boolean opCharStillUsed = false;

            for(OperatorGroup og : operatorGroups)
                if(og.postfixOperators.remove(uop))
                    break;

            ogloop:
            for(OperatorGroup og: operatorGroups)
            {
                for(UnaryOperator sop : og.prefixOperators)
                    if(sop.lex == operator)
                    {
                        opCharStillUsed = true;
                        break ogloop;
                    }

                for(BinaryOperator bop : og.leftAssociativeBinaryOperators)
                    if(bop.lex == operator)
                    {
                        opCharStillUsed = true;
                        break ogloop;
                    }

                for(BinaryOperator bop : og.rightAssociativeBinaryOperators)
                    if(bop.lex == operator)
                    {
                        opCharStillUsed = true;
                        break ogloop;
                    }
            }

            if(!opCharStillUsed)
                operatorChars.remove(operator);
        }

        return this;
    }

    /**
     * Stops all current variables from being usable in the equation.
     * @return This equation evaluation.
     */
    public EquationEvaluation withClearedVariables()
    {
        variableValues.clear();
        return this;
    }

    /**
     * Stops all current functions from being usable in the equation.
     * @return This equation evaluation.
     */
    public EquationEvaluation withClearedFunctions()
    {
        functionMap.clear();
        return this;
    }

    /**
     * Stops all current operators from being usable in the equation.
     * @return This equation evaluation.
     */
    public EquationEvaluation withClearedOperators()
    {
        operatorGroups.clear();
        operatorChars.clear();
        binaryOperators.clear();
        prefixOperators.clear();
        postfixOperators.clear();
        return this;
    }

    /**
     * Stops all current variables, functions, and operators from being usable in the equation.
     * @return This equation evaluation.
     */
    public EquationEvaluation withClearedFunctionality()
    { return this.withClearedVariables().withClearedFunctions().withClearedOperators(); }
    //endregion
    //endregion
}
