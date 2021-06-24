package scot.massie.lib.maths;

import com.sun.istack.internal.NotNull;
import scot.massie.lib.collections.maps.FallbackMap;
import scot.massie.lib.collections.tree.RecursiveTree;
import scot.massie.lib.collections.tree.Tree;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.function.DoubleSupplier;
import java.util.function.ToDoubleBiFunction;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;

import static scot.massie.lib.utils.ControlFlowUtils.*;

/**
 * <p>An equation compiled from a string representation of itself, which may be evaluated at any time.</p>
 *
 * <p>Equations may be created using the constructor accepting a string, which parses the string as an equation using
 * the default variables, functions, and operators, or with the {@link Builder} class, which will allow the use of
 * custom variables, functions, and operations.</p>
 *
 * <p>Variables and functions used in the equation may be redefined at any point, with
 * {@link Equation#setVariable(String, double) .setVariable(...)} and
 * {@link Equation#redefineFunction(String, ToDoubleFunction) .redefineFunction}.</p>
 */
public class Equation
{
    //region inner classes
    //region exceptions
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

        /**
         * The name of the function that requires more arguments.
         */
        final String functionName;

        /**
         * The number of arguments that are required to be passed to the function.
         */
        final int numberOfArgsRequired;

        /**
         * The number of arguments that actually were passed to the function.
         */
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

    /**
     * Class for constructing instances of {@link Equation}. Allows the definition of operators, functions, and
     * variables for use in equations.
     */
    public static final class Builder
    {
        //region inner classes
        //region exceptions

        /**
         * Thrown when an equation provided as a string cannot be compiled into an equation.
         */
        public static class EquationParseException extends RuntimeException
        {
            /**
             * The specific section of the equation causing this exception.
             */
            final TokenList equationSection;

            /**
             * The full equation causing this equation.
             */
            final TokenList fullEquation;

            /**
             * Creates a new EquationParseException.
             * @param fullEquation The full equation causing this exception, as a TokenList.
             * @param equationSection The specific section of the equation causing this exception, as a TokenList.
             */
            EquationParseException(TokenList fullEquation, TokenList equationSection)
            {
                super("Equation was not parsable as an equation: " + fullEquation.equationAsString
                      + "\nSpecifically, this portion: " + equationSection.equationAsString);

                this.fullEquation = fullEquation;
                this.equationSection = equationSection;
            }

            /**
             * Creates a new EquationParseException.
             * @param fullEquation The full equation causing this exception, as a TokenList.
             * @param equationSection The specific section of the equation causing this exception, as a TokenList.
             * @param msg The exception message.
             */
            EquationParseException(TokenList fullEquation, TokenList equationSection, String msg)
            {
                super(msg);
                this.fullEquation = fullEquation;
                this.equationSection = equationSection;
            }

            /**
             * Gets the specific section of the equation that caused this exception.
             * @return The specific substring of the given equation that caused this exception.
             */
            public String getEquationSection()
            { return equationSection.equationAsString.trim(); }

            /**
             * Gets the equation that caused this exception.
             * @return The full equation as a string, that caused this exception.
             */
            public String getFullEquation()
            { return fullEquation.equationAsString.trim(); }

            /**
             * Copies this exception with a different given full equation. This allows the exception to be caught and
             * rethrown with a fuller "full equation" value.
             * @param fullEquation The full equation causing this exception, as a TokenList.
             * @return A copy of this exception, with the full equation set to the given TokenList. The equation section
             *         remains the same.
             */
            public EquationParseException withFullEquation(TokenList fullEquation)
            { return new EquationParseException(fullEquation, equationSection); }
        }

        /**
         * Thrown when an equation (or section thereof) starts with or ends with an operator than cannot be a prefix or
         * postfix operator respectively.
         */
        public static class DanglingOperatorException extends EquationParseException
        {
            /**
             * Creates a new DanglingOperatorException.
             * @param fullEquation The full equation causing this exception, as a TokenList.
             * @param equationSection The specific section of the equation causing this exception, as a TokenList.
             */
            public DanglingOperatorException(TokenList fullEquation, TokenList equationSection)
            {
                super(fullEquation, equationSection,
                      "Equation contained a dangling operator that could not be a prefix nor postfix operator: "
                        + fullEquation.equationAsString
                        + "\nSpecifically, this portion: " + equationSection.equationAsString);
            }

            /**
             * Creates a new DanglingOperatorException.
             * @param fullEquation The full equation causing this exception, as a TokenList.
             * @param equationSection The specific section of the equation causing this exception, as a TokenList.
             * @param msg The exception message
             */
            public DanglingOperatorException(TokenList fullEquation, TokenList equationSection, String msg)
            { super(fullEquation, equationSection, msg); }

            @Override
            public DanglingOperatorException withFullEquation(TokenList fullEquation)
            { return new DanglingOperatorException(fullEquation, equationSection); }
        }

        /**
         * Thrown when an equation (or section thereof) starts with an operator that cannot be a prefix operator.
         */
        public static class LeadingNonPrefixOperatorException extends DanglingOperatorException
        {
            /**
             * Creates a new LeadingNonPrefixOperatorException.
             * @param fullEquation The full equation causing this exception, as a TokenList.
             * @param equationSection The specific section of the equation causing this exception, as a TokenList.
             */
            public LeadingNonPrefixOperatorException(TokenList fullEquation, TokenList equationSection)
            {
                super(fullEquation, equationSection,
                      "Equation contained a leading operator that could not be a prefix operator: "
                      + fullEquation.equationAsString
                      + "\nSpecifically, this portion: " + equationSection.equationAsString);
            }

            /**
             * Creates a new LeadingNonPrefixOperatorException.
             * @param fullEquation The full equation causing this exception, as a TokenList.
             * @param equationSection The specific section of the equation causing this exception, as a TokenList.
             * @param msg The exception message
             */
            public LeadingNonPrefixOperatorException(TokenList fullEquation, TokenList equationSection, String msg)
            { super(fullEquation, equationSection, msg); }

            @Override
            public LeadingNonPrefixOperatorException withFullEquation(TokenList fullEquation)
            { return new LeadingNonPrefixOperatorException(fullEquation, equationSection); }
        }

        /**
         * Thrown when an equation (or section thereof) ends with an operator that cannot be a postfix operator.
         */
        public static class TrailingNonPostfixOperatorException extends DanglingOperatorException
        {
            /**
             * Creates a new TrailingNonPostfixOperatorException.
             * @param fullEquation The full equation causing this exception, as a TokenList.
             * @param equationSection The specific section of the equation causing this exception, as a TokenList.
             */
            public TrailingNonPostfixOperatorException(TokenList fullEquation, TokenList equationSection)
            {
                super(fullEquation, equationSection,
                      "Equation contained a trailing operator that could not be a postfix operator: "
                      + fullEquation.equationAsString
                      + "\nSpecifically, this portion: " + equationSection.equationAsString);
            }

            /**
             * Creates a new TrailingNonPostfixOperatorException.
             * @param fullEquation The full equation causing this exception, as a TokenList.
             * @param equationSection The specific section of the equation causing this exception, as a TokenList.
             * @param msg The exception message
             */
            public TrailingNonPostfixOperatorException(TokenList fullEquation, TokenList equationSection, String msg)
            { super(fullEquation, equationSection, msg); }

            @Override
            public TrailingNonPostfixOperatorException withFullEquation(TokenList fullEquation)
            { return new TrailingNonPostfixOperatorException(fullEquation, equationSection); }
        }

        /**
         * Thrown when a list of arguments in a function call starts or ends with an argument separator. (A comma)
         */
        public static class DanglingArgumentSeparatorException extends EquationParseException
        {
            /**
             * Creates a new DanglingArgumentSeparatorException.
             * @param fullEquation The full equation causing this exception, as a TokenList.
             * @param equationSection The specific section of the equation causing this exception, as a TokenList.
             */
            public DanglingArgumentSeparatorException(TokenList fullEquation, TokenList equationSection)
            {
                super(fullEquation, equationSection,
                      "Equation contained an argument list with a dangling separator: "
                      + fullEquation.equationAsString
                      + "\nSpecifically, this portion: " + equationSection.equationAsString);
            }

            /**
             * Creates a new DanglingArgumentSeparatorException.
             * @param fullEquation The full equation causing this exception, as a TokenList.
             * @param equationSection The specific section of the equation causing this exception, as a TokenList.
             * @param msg The exception message
             */
            public DanglingArgumentSeparatorException(TokenList fullEquation, TokenList equationSection, String msg)
            { super(fullEquation, equationSection, msg); }

            @Override
            public DanglingArgumentSeparatorException withFullEquation(TokenList fullEquation)
            { return new DanglingArgumentSeparatorException(fullEquation, equationSection); }
        }

        /**
         * Thrown when a list of arguments in a function call starts with an argument separator. (A comma)
         */
        public static class LeadingArgumentSeparatorException extends DanglingArgumentSeparatorException
        {
            /**
             * Creates a new LeadingArgumentSeparatorException.
             * @param fullEquation The full equation causing this exception, as a TokenList.
             * @param equationSection The specific section of the equation causing this exception, as a TokenList.
             */
            public LeadingArgumentSeparatorException(TokenList fullEquation, TokenList equationSection)
            {
                super(fullEquation, equationSection,
                      "Equation contained an argument list with a leading separator: "
                      + fullEquation.equationAsString
                      + "\nSpecifically, this portion: " + equationSection.equationAsString);
            }

            /**
             * Creates a new LeadingArgumentSeparatorException.
             * @param fullEquation The full equation causing this exception, as a TokenList.
             * @param equationSection The specific section of the equation causing this exception, as a TokenList.
             * @param msg The exception message
             */
            public LeadingArgumentSeparatorException(TokenList fullEquation, TokenList equationSection, String msg)
            { super(fullEquation, equationSection, msg); }

            @Override
            public LeadingArgumentSeparatorException withFullEquation(TokenList fullEquation)
            { return new LeadingArgumentSeparatorException(fullEquation, equationSection); }
        }

        /**
         * Thrown when a list of arguments in a function call ends with an argument separator. (A comma)
         */
        public static class TrailingArgumentSeparatorException extends DanglingArgumentSeparatorException
        {
            /**
             * Creates a new TrailingArgumentSeparatorException.
             * @param fullEquation The full equation causing this exception, as a TokenList.
             * @param equationSection The specific section of the equation causing this exception, as a TokenList.
             */
            public TrailingArgumentSeparatorException(TokenList fullEquation, TokenList equationSection)
            {
                super(fullEquation, equationSection,
                      "Equation contained an argument list with a trailing separator: "
                      + fullEquation.equationAsString
                      + "\nSpecifically, this portion: " + equationSection.equationAsString);
            }

            /**
             * Creates a new TrailingArgumentSeparatorException.
             * @param fullEquation The full equation causing this exception, as a TokenList.
             * @param equationSection The specific section of the equation causing this exception, as a TokenList.
             * @param msg The exception message
             */
            public TrailingArgumentSeparatorException(TokenList fullEquation, TokenList equationSection, String msg)
            { super(fullEquation, equationSection, msg); }

            @Override
            public TrailingArgumentSeparatorException withFullEquation(TokenList fullEquation)
            { return new TrailingArgumentSeparatorException(fullEquation, equationSection); }
        }

        /**
         * Thrown when an argument in a function call is empty. (e.g. it's two consecutive commas)
         */
        public static class EmptyFunctionArgumentException extends EquationParseException
        {
            /**
             * Creates a new EmptyFunctionArgumentException.
             * @param fullEquation The full equation causing this exception, as a TokenList.
             * @param equationSection The specific section of the equation causing this exception, as a TokenList.
             */
            public EmptyFunctionArgumentException(TokenList fullEquation, TokenList equationSection)
            {
                super(fullEquation, equationSection,
                      "Equation contained an empty argument to a function. That is, argument separators were used, but "
                      + "an argument wasn't passed in:"
                      + fullEquation.equationAsString
                      + "\nSpecifically, this portion: " + equationSection.equationAsString);
            }

            /**
             * Creates a new EmptyFunctionArgumentException.
             * @param fullEquation The full equation causing this exception, as a TokenList.
             * @param equationSection The specific section of the equation causing this exception, as a TokenList.
             * @param msg The exception message
             */
            public EmptyFunctionArgumentException(TokenList fullEquation, TokenList equationSection, String msg)
            { super(fullEquation, equationSection, msg); }
        }

        /**
         * Thrown when not all open brackets in an equation match with a closing bracket, or vice versa.
         */
        public static class BracketMismatchException extends EquationParseException
        {
            /**
             * Creates a new BracketMismatchException.
             * @param equation The full equation causing this exception, as a TokenList.
             */
            public BracketMismatchException(TokenList equation)
            { super(equation, equation, "Equation contained a bracket mismatch: " + equation.equationAsString); }

            /**
             * Creates a new BracketMismatchException.
             * @param equation The full equation causing this exception, as a TokenList.
             * @param msg The exception message
             */
            public BracketMismatchException(TokenList equation, String msg)
            { super(equation, equation, msg); }

            @Override
            public BracketMismatchException withFullEquation(TokenList fullEquation)
            { return new BracketMismatchException(fullEquation); }
        }

        /**
         * Scanning the equation, thrown when a close bracket is encountered without matching an open bracket.
         */
        public static class UnexpectedCloseBracketException extends BracketMismatchException
        {
            /**
             * Creates a new UnexpectedCloseBracketException.
             * @param equation The full equation causing this exception, as a TokenList.
             */
            public UnexpectedCloseBracketException(TokenList equation)
            {
                super(equation, "Equation contained a close bracket that didn't correlate to a matching open bracket: "
                                + equation.equationAsString);
            }

            /**
             * Creates a new UnexpectedCloseBracketException.
             * @param equation The full equation causing this exception, as a TokenList.
             * @param msg The exception message
             */
            public UnexpectedCloseBracketException(TokenList equation, String msg)
            { super(equation, msg); }

            @Override
            public UnexpectedCloseBracketException withFullEquation(TokenList fullEquation)
            { return new UnexpectedCloseBracketException(fullEquation); }
        }

        /**
         * Thrown when an open bracket is never matched with a close bracket.
         */
        public static class UnmatchedOpenBracketException extends BracketMismatchException
        {
            /**
             * Creates a new UnmatchedOpenBracketException.
             * @param equation The full equation causing this exception, as a TokenList.
             */
            public UnmatchedOpenBracketException(TokenList equation)
            {
                super(equation, "Equation contained an open bracket that didn't correlate to a matching close bracket: "
                                + equation.equationAsString);
            }

            /**
             * Creates a new UnmatchedOpenBracketException.
             * @param equation The full equation causing this exception, as a TokenList.
             * @param msg The exception message
             */
            public UnmatchedOpenBracketException(TokenList equation, String msg)
            { super(equation, msg); }

            @Override
            public UnmatchedOpenBracketException withFullEquation(TokenList fullEquation)
            { return new UnmatchedOpenBracketException(fullEquation); }
        }

        /**
         * Thrown when a function is referenced that hasn't been defined.
         */
        public static class UnrecognisedFunctionException extends EquationParseException
        {
            /**
             * Creates a new UnrecognisedFunctionException.
             * @param functionName The name of the function that was called, but isn't defined.
             * @param fullEquation The full equation causing this exception, as a TokenList.
             * @param equationSection The specific section of the equation causing this exception, as a TokenList.
             */
            public UnrecognisedFunctionException(String functionName,
                                                 TokenList fullEquation,
                                                 TokenList equationSection)
            {
                super(fullEquation, equationSection, "Equation contained an unrecognised function: " + functionName);
                this.functionName = functionName;
            }

            /**
             * Creates a new UnrecognisedFunctionException.
             * @param functionName The name of the function that was called, but isn't defined.
             * @param fullEquation The full equation causing this exception, as a TokenList.
             * @param equationSection The specific section of the equation causing this exception, as a TokenList.
             * @param msg The exception message
             */
            public UnrecognisedFunctionException(String functionName,
                                                 TokenList fullEquation,
                                                 TokenList equationSection,
                                                 String msg)
            {
                super(fullEquation, equationSection, msg);
                this.functionName = functionName;
            }

            /**
             * The name of the function that was called without being defined.
             */
            String functionName;

            /**
             * Gets the name of the function that was called without being defined.
             * @return The name of the function that was called without being defined.
             */
            public String getFunctionName()
            { return functionName; }

            @Override
            public UnrecognisedFunctionException withFullEquation(TokenList fullEquation)
            { return new UnrecognisedFunctionException(functionName, fullEquation, equationSection); }
        }
        //endregion

        /**
         * A store of operators with the same priority level.
         */
        static class OperatorPriorityGroup
        {
            final Map<Token, PrefixOperator> prefixOperators = new HashMap<>();
            final Map<Token, PostfixOperator> postfixOperators = new HashMap<>();
            final Tree<Token, InfixOperator> leftAssociativeInfixOperators = new RecursiveTree<>();
            final Tree<Token, InfixOperator> rightAssociativeInfixOperators = new RecursiveTree<>();
        }

        /**
         * A token list and information about how it was derived. This represents a token list being derived from
         * getting tokens before and after a particular token in another {@link TokenList}.
         */
        static class OperatorTokenRun
        {
            /**
             * The index in the original token list at which this token run starts.
             */
            int startIndexInSource;

            /**
             * The index in the original token list at which this token run ends. (inclusive)
             */
            int endIndexInSource;

            /**
             * The indext in this token run of the token around which this token run was formed.
             */
            int indexOfPivotInRun;

            /**
             * The tokens in this token run.
             */
            List<Token> tokens;

            /**
             * The tokens in this token run before the pivot.
             */
            List<Token> tokensBeforePivot;

            /**
             * The tokens in this token run after the pivot.
             */
            List<Token> tokensAfterPivot;

            /**
             * Creates a new OperatorTokenRun.
             * @param source The token list to this should be derived from.
             * @param runStartIndex The index in the given token list at which the derived token list should start.
             * @param runEndIndex The index in the given token list at which the derived token list should end.
             * @param pivotIndexInSource The index in the given token list of the token around which this token run was
             *                           formed.
             */
            public OperatorTokenRun(List<Token> source, int runStartIndex, int runEndIndex, int pivotIndexInSource)
            {
                this.startIndexInSource = runStartIndex;
                this.endIndexInSource = runEndIndex;
                this.indexOfPivotInRun = pivotIndexInSource - runStartIndex;
                this.tokens = source.subList(runStartIndex, runEndIndex + 1);
                this.tokensBeforePivot = source.subList(runStartIndex, pivotIndexInSource);
                this.tokensAfterPivot = source.subList(pivotIndexInSource + 1, runEndIndex + 1);
            }
        }
        //endregion

        //region constants
        /**
         * The default operator priority. If no priority value is provided, this is used instead.
         */
        static final double DEFAULT_PRIORITY = 0;

        /**
         * The default operator associativity. If an associativity is not specified, this is used instead.
         */
        static final boolean DEFAULT_ASSOCIATIVITY = true; // true == left, false == right.

        /**
         * The mathematical constant Phi, or the golen ratio.
         * @see <a href="https://en.wikipedia.org/wiki/Golden_ratio">Wikipedia: The golden ratio.</a>
         */
        static final double PHI = (1 + Math.sqrt(5)) / 2;
        //endregion

        //region variables
        /**
         * Fixed string tokens that may appear in an equation. Does not include variable names, function names,
         * numbers, or unparsable portions of equation.
         */
        Set<Token> possibleTokens = new HashSet<>();
        {
            possibleTokens.add(Token.OPEN_BRACKET);
            possibleTokens.add(Token.CLOSE_BRACKET);
            possibleTokens.add(Token.ARGUMENT_SEPARATOR);
        }

        /**
         * {@link #possibleTokens}, but in order from first registered to last registered. This is the reverse of the
         * order in which equations are tokenised. It should be noted that where a token whose text contains the text of
         * other tokens is registered earlier (and thus tokenised later), matching strings will be parsed as the smaller
         * tokens rather than the larger ones, as the smaller ones will be considered first.
         */
        List<Token> possibleTokensInOrder = new ArrayList<>();
        {
            possibleTokensInOrder.add(Token.OPEN_BRACKET);
            possibleTokensInOrder.add(Token.CLOSE_BRACKET);
            possibleTokensInOrder.add(Token.ARGUMENT_SEPARATOR);
        }

        /**
         * Tokens used by operators registered to this builder.
         */
        Set<Token> operatorTokens = new HashSet<>();

        /**
         * Tokens used by infix operators registered to this builder.
         */
        Set<Token> infixOperatorTokens = new HashSet<>();

        /**
         * Infix operators. The operator definitions themselves, using the list of tokens used to invoke them in order
         * in a list as the key.
         */
        Map<List<Token>, InfixOperator>         infixOperators      = new HashMap<>();

        /**
         * Prefix operators. The operator definitions themselves, using the tokens used to prefix an operand to invoke
         * them.
         */
        Map<Token, PrefixOperator>              prefixOperators     = new HashMap<>();

        /**
         * Postfix operators. The operator definitions themselves, using the tokens used to postfix an operand to invoke
         * them.
         */
        Map<Token, PostfixOperator>             postfixOperators    = new HashMap<>();

        /**
         * Functions available to equations. The function that is called when the function is invoked in an equation,
         * mapped against the name of the equation which may be used to invoke them.
         */
        Map<String, ToDoubleFunction<double[]>> functions           = new HashMap<>();

        /**
         * Variables available to equations. The variable's value mapped against the name of the variable as it may be
         * referred to as in an equation.
         */
        Map<String, Double>                     variables           = new HashMap<>();


        /**
         * The operators in this builder, arranged into groups by priority, indexed against those operator priorities.
         * This is initialised when an equation needs to be built (if it isn't already), and is invalidated when a new
         * operator is registered.
         */
        Map<Double, OperatorPriorityGroup> operatorGroups        = null;

        /**
         * The operators in this builder, arranged into groups by priority, in order of priority from lowest to highest.
         * This is initialised when an equation needs to be built (if it isn't already), and is invalidated when a new
         * operator is registered.
         */
        List<OperatorPriorityGroup>        operatorGroupsInOrder = null;
        //endregion

        /**
         * Instances of {@link Equation} created by this builder not yet picked up by the garbage collector. This allows
         * updates to be pushed to these objects.
         */
        Set<Equation> instances = Collections.newSetFromMap(new WeakHashMap<>());

        //region initialisation

        /**
         * Creates a new equation builder, initialised with the default operators, functions, and variables.
         * @see #withDefaultOperators()
         * @see #withDefaultFunctions() 
         * @see #withDefaultVariables()
         */
        public Builder()
        { this(true); }

        /**
         * Creates a new equation builder.
         * @param includeDefaults Whether or not to include the default operators, functions, and variables. See
         *                        {@link #withDefaultOperators()}, {@link #withDefaultFunctions()}, and
         *                        {@link #withDefaultVariables()}.
         */
        public Builder(boolean includeDefaults)
        {
            if(includeDefaults)
            {
                addDefaultOperators();
                addDefaultFunctions();
                addDefaultVariables();
            }
        }
        //endregion

        //region methods
        //region add functionality
        //region internal
        //region add groups of functionality
        /**
         * Adds the default operators to this builder.
         */
        void addDefaultOperators()
        {
            withOperator        ("-",             100,  (l, r)    -> l - r);
            withOperator        ("+",             100,  (l, r)    -> l + r);
            withOperator        ("/",             200,  (l, r)    -> l / r);
            withOperator        ("÷",             200,  (l, r)    -> l / r);
            withOperator        ("*",             200,  (l, r)    -> l * r);
            withOperator        ("×",             200,  (l, r)    -> l * r);
            withOperator        ("%",             300,  (l, r)    -> l % r);
            withPrefixOperator  ("-",             500,  x         -> -x);
            withPrefixOperator  ("+",             500,  x         -> +x);
            withOperator        ("√",      false, 600,  (l, r)    -> Math.pow(r, 1.0 / l));
            withPrefixOperator  ("√",             700,  x         -> Math.sqrt(x));
            withOperator        ("^",      false, 800,  (l, r)    -> Math.pow(l, r));
            withPostfixOperator ("%",             900,  x         -> x / 100);
        }

        /**
         * Adds comparative and conditional operators to this builder.
         */
        void addComparativeOperators()
        {
            withPrefixOperator  ("!",          -100, x      -> x >= 0.5 ? 0 : 1);

            withOperator        ("<",   true,  -200, (l, r) -> l < r ? 1 : 0);
            withOperator        (">",   true,  -200, (l, r) -> l > r ? 1 : 0);
            withOperator        ("<=",  true,  -200, (l, r) -> l <= r ? 1 : 0);
            withOperator        ("≤",   true,  -200, (l, r) -> l <= r ? 1 : 0);
            withOperator        (">=",  true,  -200, (l, r) -> l >= r ? 1 : 0);
            withOperator        ("≥",   true,  -200, (l, r) -> l >= r ? 1 : 0);

            withOperator        ("=",   true,  -300, (l, r) ->
            {
                double delta = Math.ulp(l) * 2;
                return (r > l - delta) && (r < l + delta) ? 1 : 0;
            });

            BinaryOperatorAction notEqualTo = (l, r) ->
            {
                double delta = Math.ulp(l) * 2;
                return (r < l - delta) || (r > l + delta) ? 1 : 0;
            };

            withOperator        ("!=",  true,  -300, notEqualTo);
            withOperator        ("≠",   true,  -300, notEqualTo);
            withOperator        ("=/=", true,  -300, notEqualTo);

            withOperator        ("&&",  true,  -400, (l, r) -> (l >= 0.5) && (r >= 0.5) ? 1 : 0);
            withOperator        ("∧",   true,  -400, (l, r) -> (l >= 0.5) && (r >= 0.5) ? 1 : 0);
            withOperator        ("⋀",   true,  -400, (l, r) -> (l >= 0.5) && (r >= 0.5) ? 1 : 0);
            withOperator        ("⋏",   true,  -400, (l, r) -> (l >= 0.5) && (r >= 0.5) ? 1 : 0);

            withOperator        ("||",  true,  -500, (l, r) -> (l >= 0.5) || (r >= 0.5) ? 1 : 0);
            withOperator        ("∨",   true,  -500, (l, r) -> (l >= 0.5) || (r >= 0.5) ? 1 : 0);
            withOperator        ("⋁",   true,  -500, (l, r) -> (l >= 0.5) || (r >= 0.5) ? 1 : 0);
            withOperator        ("⋎",   true,  -500, (l, r) -> (l >= 0.5) || (r >= 0.5) ? 1 : 0);

            withOperator("?", ":", false, -600, (a, b, c) -> a >= 0.5 ? b : c);
        }

        /**
         * Adds the default functions to this builder.
         */
        void addDefaultFunctions()
        {
            withMonoFunction("cos",      Math::cos);
            withMonoFunction("sin",      Math::sin);
            withMonoFunction("tan",      Math::tan);
            withMonoFunction("sqrt",     Math::sqrt);
            withMonoFunction("cbrt",     Math::sqrt);
            withMonoFunction("log",      Math::log);
            withMonoFunction("log10",    Math::log10);

            withMonoFunction("fib", n ->
            {
                double result = (Math.pow(PHI, n) - (Math.pow(-PHI, -n))) / (Math.sqrt(5));
                return n % 1 == 0 ? Math.round(result) : result;
            });

            withMonoFunction("floor",    Math::floor);
            withMonoFunction("ceiling",  Math::ceil);
            withMonoFunction("ceil",     Math::ceil);
            withMonoFunction("truncate", Double::intValue);
            withMonoFunction("trunc",    Double::intValue);
            withMonoFunction("round",    Math::round);

            withFunction("min", 1, args ->
            {
                double min = args[0];

                for(int i = 1; i < args.length; i++)
                    if(args[i] < min)
                        min = args[i];

                return min;
            });

            withFunction("max", 1, args ->
            {
                double max = args[0];

                for(int i = 1; i < args.length; i++)
                    if(args[i] > max)
                        max = args[i];

                return max;
            });

            withFunction("avg", 1, args ->
            {
                double avg = args[0];

                for(int i = 1; i < args.length; i++)
                    avg += (args[i] - avg) / (i + 1);

                return avg;
            });

            withFunction("median", 1, args ->
            {
                Arrays.sort(args);

                if(args.length % 2 == 0)
                    return (args[args.length / 2] + args[args.length / 2 - 1]) / 2;
                else
                    return args[args.length / 2];
            });
        }

        /**
         * Adds the default variables to this builder.
         */
        void addDefaultVariables()
        {
            withVariable("π", Math.PI);
            withVariable("pi", Math.PI);
            withVariable("e", Math.E);
            withVariable("ϕ", PHI);
            withVariable("φ", PHI);
            withVariable("phi", PHI);
            withVariable("∞", Double.POSITIVE_INFINITY);
            withVariable("inf", Double.POSITIVE_INFINITY);
        }
        //endregion

        //region add single pieces of functionality

        /**
         * Adds an operator to this builder.
         * @param op The operator to add.
         */
        void addOperator(Operator op)
        {
            if(op instanceof PrefixOperator)
                addOperator((PrefixOperator)op);
            else if(op instanceof PostfixOperator)
                addOperator((PostfixOperator)op);
            else if(op instanceof InfixOperator)
                addOperator((InfixOperator)op);
            else
                throw new UnsupportedOperationException("Unrecognised operator type: " + op.getClass().getName());
        }

        /**
         * Adds a prefix operator to this builder.
         * @param op The operator to add.
         */
        void addOperator(PrefixOperator op)
        {
            invalidateOperatorGroups();
            Token popToken = op.getToken();
            prefixOperators.put(popToken, op);
            addOperatorToken(popToken);
        }

        /**
         * Adds a postfix operator to this builder.
         * @param op The operator to add.
         */
        void addOperator(PostfixOperator op)
        {
            invalidateOperatorGroups();
            Token popToken = op.getToken();
            postfixOperators.put(popToken, op);
            addOperatorToken(popToken);
        }

        /**
         * Adds an infix (binary, ternary, etc.) operator to this builder.
         * @param op The builder to add.
         */
        void addOperator(InfixOperator op)
        {
            invalidateOperatorGroups();
            List<Token> iopTokens = op.getTokens();
            infixOperators.put(iopTokens, op);
            addOperatorTokens(iopTokens);
            infixOperatorTokens.addAll(iopTokens);
        }

        /**
         * <p>Registers a token for the tokenisation phase of parsing an equation. Tokens should be added in reverse
         * order of the order they should be parsed in. That is, tokens that may contain other tokens should be added
         * after those other tokens, and where tokens overlap, the most recently added token takes precedence.</p>
         *
         * <p>In short, register tokens in order from shortest to longest.</p>
         *
         * <p>Adding an operator also registers its tokens if they're not already registered. If the order operators are
         * added in can't reflect the order tokens should be registered in, tokens can be registered separately.</p>
         *
         * <p>Behaviour is undefined where a token is registered that is never used as part of any operator.</p>
         * @param token The token to be registered.
         */
        void addOperatorToken(Token token)
        {
            if(possibleTokens.add(token))
            {
                possibleTokensInOrder.add(token);
                operatorTokens.add(token);
            }
        }

        /**
         * <p>Registers tokens for the tokenisation phase of parsing an equation. Tokens should be added in reverse
         * order of the order they should be parsed in. That is, tokens that may contain other tokens should be added
         * after those other tokens, and where tokens overlap, the most recently added token takes precedence.</p>
         *
         * <p>In short, register tokens in order from shortest to longest.</p>
         *
         * <p>Adding an operator also registers its tokens if they're not already registered. If the order operators are
         * added in can't reflect the order tokens should be registered in, tokens can be registered separately.</p>
         *
         * <p>Behaviour is undefined where a token is registered that is never used as part of any operator.</p>
         * @param tokens The tokens to be registered, in the order to register them.
         */
        void addOperatorTokens(List<Token> tokens)
        {
            for(Token token : tokens)
                if(possibleTokens.add(token))
                {
                    possibleTokensInOrder.add(token);
                    operatorTokens.add(token);
                }
        }
        //endregion
        //endregion

        //region public interface
        //region add tokens
        /**
         * <p>Registers a token for the tokenisation phase of parsing an equation. Tokens should be added in reverse
         * order of the order they should be parsed in. That is, tokens that may contain other tokens should be added
         * after those other tokens, and where tokens overlap, the most recently added token takes precedence.</p>
         *
         * <p>In short, register tokens in order from shortest to longest.</p>
         *
         * <p>Adding an operator also registers its tokens if they're not already registered. If the order operators are
         * added in can't reflect the order tokens should be registered in, tokens can be registered separately.</p>
         *
         * <p>Behaviour is undefined where a token is registered that is never used as part of any operator.</p>
         * @param token The token to be registered.
         * @return This.
         */
        public Builder withToken(String token)
        {
            addOperatorToken(new Token(token));
            return this;
        }
        //endregion

        //region add variables
        //region add groups of variables

        /**
         * <p>Defines all default variables for equations made by this builder.</p>
         *
         * <p>Adds the following variables:</p>
         * <ul>
         *     <li>π or pi</li>
         *     <li>e</li>
         *     <li>ϕ or φ or phi</li>
         *     <li>∞ or inf</li>
         * </ul>
         * @return This
         */
        public Builder withDefaultVariables()
        {
            addDefaultVariables();
            return this;
        }
        //endregion

        //region add single variables
        /**
         * Defines a variable for equations made by this builder. Variables may be accessed by addressing them by name.
         * @param name The name of the variable.
         * @param value The value of the variable.
         * @return This.
         */
        public Builder withVariable(String name, double value)
        {
            variables.put(name, value);
            return this;
        }

        /**
         * <p>Defines a variable for equations made by this builder as in {@link #withVariable(String, double)}, but
         * also pushes this change to instances of {@link Equation} created by this builder.</p>
         *
         * <p>Note that this won't override variables redefined on the equation itself. This also won't allow the use of
         * new variables in already compiled equations - this would require a new equation object to be created.</p>
         * @param name The name of the variable.
         * @param value The value of the variable.
         * @return This.
         */
        public Builder pushVariable(String name, double value)
        {
            variables.put(name, value);

            for(Equation e : instances)
                e.initialVariableValues.put(name, value);

            return this;
        }
        //endregion
        //endregion

        //region add functions
        //region add groups of functions
        /**
         * <p>Defines all default functions for equations made by this builder.</p>
         *
         * <p>Adds the following functions:</p>
         * <ul>
         *     <li>cos</li>
         *     <li>sin</li>
         *     <li>tan</li>
         *     <li>sqrt</li>
         *     <li>cbrt</li>
         *     <li>log</li>
         *     <li>log10</li>
         *     <li>fib</li>
         *     <li>floor</li>
         *     <li>ceiling</li>
         *     <li>ceil</li>
         *     <li>truncate</li>
         *     <li>trunc</li>
         *     <li>round</li>
         *     <li>min</li>
         *     <li>max</li>
         *     <li>avg</li>
         *     <li>median</li>
         * </ul>
         * @return This
         */
        public Builder withDefaultFunctions()
        {
            addDefaultFunctions();
            return this;
        }
        //endregion

        //region add single functions
        /**
         * Defines a function for equations made by this builder. Functions may be invoked by addressing them by name,
         * followed by a comma-separated list of arguments (equations) enclosed in (brackets).
         * @param name The name of the function.
         * @param f The implementation of a function. Arguments to the function are passed into the implementation as an
         *          array of doubles.
         * @return This.
         */
        public Builder withFunction(String name, ToDoubleFunction<double[]> f)
        {
            functions.put(name, f);
            return this;
        }

        /**
         * Defines a function for equations made by this builder. Functions may be invoked by addressing them by name,
         * followed by a pair of matching brackets().
         * @param name The name of the function.
         * @param f The implementation of a function, not taking any arguments.
         * @return This.
         */
        public Builder withFunction(String name, DoubleSupplier f)
        { return withFunction(name, value -> f.getAsDouble()); }

        /**
         * Defines a function for equations made by this builder. Functions may be invoked by addressing them by name,
         * followed by a comma-separated list of arguments (equations) enclosed in (brackets).
         * @param name The name of the function.
         * @param requiredArgCount The number of arguments required to be passed into this function. Where the equation
         *                         invokes the function without this number of arguments passed in, a
         *                         {@link MissingFunctionArgumentsException} is thrown.
         * @param f The implementation of a function. Arguments to the function are passed into the implementation as an
         *          array of doubles.
         * @return This.
         */
        public Builder withFunction(String name, int requiredArgCount, ToDoubleFunction<double[]> f)
        {
            return withFunction(name, args ->
            {
                if(args.length < requiredArgCount)
                    throw new MissingFunctionArgumentsException(name, requiredArgCount, args.length);

                return f.applyAsDouble(args);
            });
        }

        /**
         * Defines a function for equations made by this builder, only accepting a single argument. (Additional
         * arguments are ignored if provided) Functions may be invoked by addressing them by name, followed by an
         * argument (equation) enclosed in (brackets).
         * @param name The name of the function.
         * @param f The implementation of the function. The first argument to the function is passed into the
         *          implementation as the first argument. (All others are discarded)
         * @return This.
         */
        public Builder withMonoFunction(String name, ToDoubleFunction<Double> f)
        {
            return withFunction(name, args ->
            {
                if(args.length < 1)
                    throw new MissingFunctionArgumentsException(name, 1, args.length);

                return f.applyAsDouble(args[0]);
            });
        }

        /**
         * Defines a function for equations made by this builder, only accepting two arguments. (Additional arguments
         * are ignored if provided) Functions may be invoked by addressing them by name, followed by a comma-separated
         * pair of arguments (equations) enclosed in (brackets).
         * @param name The name of the function.
         * @param f The implementation of the function. The first argument to the function is passed into the
         *          implementation as the first argument, and the second argument to the function is passed into the
         *          implementation as the second argument. (All others are discarded)
         * @return This.
         */
        public Builder withBiFunction(String name, ToDoubleBiFunction<Double, Double> f)
        {
            return withFunction(name, args ->
            {
                if(args.length < 2)
                    throw new MissingFunctionArgumentsException(name, 2, args.length);

                return f.applyAsDouble(args[0], args[1]);
            });
        }

        /**
         * <p>Defines a function for equations made by this builder as in
         * {@link #withFunction(String, ToDoubleFunction)}, but also pushes this change to instances of {@link Equation}
         * created by this builder.</p>
         *
         * <p>Note that this won't override variables redefined on the equation itself. This also won't allow the use of
         * new functions in already compiled equations - this would require a new equation object to be created.</p>
         * @param name The name of the function.
         * @param f The implementation of a function. Arguments to the function are passed into the implementation as an
         *          array of doubles.
         * @return This.
         */
        public Builder pushFunction(String name, ToDoubleFunction<double[]> f)
        {
            functions.put(name, f);

            for(Equation e : instances)
                e.initialFunctions.put(name, f);

            return this;
        }

        /**
         * <p>Defines a function for equations made by this builder as in
         * {@link #withFunction(String, DoubleSupplier)}, but also pushes this change to instances of {@link Equation}
         * created by this builder.</p>
         *
         * <p>Note that this won't override variables redefined on the equation itself. This also won't allow the use of
         * new functions in already compiled equations - this would require a new equation object to be created.</p>
         * @param name The name of the function.
         * @param f The implementation of a function, not taking any arguments.
         * @return This.
         */
        public Builder pushFunction(String name, DoubleSupplier f)
        { return pushFunction(name, x -> f.getAsDouble()); }

        /**
         * <p>Defines a function for equations made by this builder as in
         * {@link #withFunction(String, int, ToDoubleFunction)}, but also pushes this change to instances of
         * {@link Equation} created by this builder.</p>
         *
         * <p>Note that this won't override variables redefined on the equation itself. This also won't allow the use of
         * new functions in already compiled equations - this would require a new equation object to be created.</p>
         * @param name The name of the function.
         * @param requiredArgCount The number of arguments required to be passed into this function. Where the equation
         *                         invokes the function without this number of arguments passed in, a
         *                         {@link MissingFunctionArgumentsException} is thrown.
         * @param f The implementation of a function. Arguments to the function are passed into the implementation as an
         *          array of doubles.
         * @return This.
         */
        public Builder pushFunction(String name, int requiredArgCount, ToDoubleFunction<double[]> f)
        {
            return pushFunction(name, args ->
            {
                if(args.length < requiredArgCount)
                    throw new MissingFunctionArgumentsException(name, requiredArgCount, args.length);

                return f.applyAsDouble(args);
            });
        }

        /**
         * <p>Defines a function for equations made by this builder as in
         * {@link #withFunction(String, ToDoubleFunction)}, but also pushes this change to instances of {@link Equation}
         * created by this builder.</p>
         *
         * <p>Note that this won't override variables redefined on the equation itself. This also won't allow the use of
         * new functions in already compiled equations - this would require a new equation object to be created.</p>
         * @param name The name of the function.
         * @param f The implementation of the function. The first argument to the function is passed into the
         *          implementation as the first argument. (All others are discarded)
         * @return This.
         */
        public Builder pushMonoFunction(String name, ToDoubleFunction<Double> f)
        {
            return pushFunction(name, args ->
            {
                if(args.length < 1)
                    throw new MissingFunctionArgumentsException(name, 1, args.length);

                return f.applyAsDouble(args[0]);
            });
        }

        /**
         * <p>Defines a function for equations made by this builder as in
         * {@link #withFunction(String, ToDoubleFunction)}, but also pushes this change to instances of {@link Equation}
         * created by this builder.</p>
         *
         * <p>Note that this won't override variables redefined on the equation itself. This also won't allow the use of
         * new functions in already compiled equations - this would require a new equation object to be created.</p>
         * @param name The name of the function.
         * @param f The implementation of the function. The first argument to the function is passed into the
         *          implementation as the first argument, and the second argument to the function is passed into the
         *          implementation as the second argument. (All others are discarded)
         * @return This.
         */
        public Builder pushBiFunction(String name, ToDoubleBiFunction<Double, Double> f)
        {
            return pushFunction(name, args ->
            {
                if(args.length < 2)
                    throw new MissingFunctionArgumentsException(name, 2, args.length);

                return f.applyAsDouble(args[0], args[1]);
            });
        }
        //endregion
        //endregion

        //region add operators
        //region add groups of operators

        /**
         * <p>Defines all default operators for equations made by this builder.</p>
         *
         * <p>Adds the following operators: </p>
         * <pre>
         *     |     Name     | Operator |   Type   | Precedence | Associativity |
         *     -------------------------------------------------------------------
         *     | Percent      | %        | Postfix  |    900     |               |
         *     | Exponent     | ^        | Binary   |    800     |     Right     |
         *     | Square root  | √        | Prefix   |    700     |               |
         *     | Nth root     | √        | Binary   |    600     |     Right     |
         *     | Positive     | +        | Prefix   |    500     |               |
         *     | Negative     | -        | Prefix   |    500     |               |
         *     | Modulo       | %        | Binary   |    300     |     Left      |
         *     | Multiply     | * or ×   | Binary   |    200     |     Left      |
         *     | Divide       | / or ÷   | Binary   |    200     |     Left      |
         *     | Add          | +        | Binary   |    100     |     Left      |
         *     | Subtract     | -        | Binary   |    100     |     Left      |
         *     -------------------------------------------------------------------
         * </pre>
         * @return This
         */
        public Builder withDefaultOperators()
        {
            addDefaultOperators();
            return this;
        }

        /**
         * <p>Defines comparative operators for equations made by this builder.</p>
         *
         * <p>Note that these operators treat 0 as false and 1 as true. Or more specifically, any number greater than or
         * equal to 0.5 as true and all other values as false.</p>
         *
         * <p>Adds the following operators: </p>
         * <pre>
         *     |          Name            |      Operator      |   Type   | Precedence | Associativity |
         *     -----------------------------------------------------------------------------------------
         *     | Not                      | !                  | Prefix   |    -100    |               |
         *     | Less than                | <                  | Binary   |    -200    |     Left      |
         *     | Greater than             | >                  | Binary   |    -200    |     Left      |
         *     | Less than or equal to    | <= or ≤            | Binary   |    -200    |     Left      |
         *     | Greater than or equal to | >= or ≥            | Binary   |    -200    |     Left      |
         *     | Equal to                 | =                  | Binary   |    -300    |     Left      |
         *     | Not equal to             | != or ≠ or =/=     | Binary   |    -300    |     Left      |
         *     | And                      | && or ∧ or ⋀ or ⋏  | Binary   |    -400    |     Left      |
         *     | Or                       | || or ∨ or ⋁ or ⋎  | Binary   |    -500    |     Left      |
         *     | Conditional              | ? :                | Ternary  |    -600    |     Right     |
         *     -----------------------------------------------------------------------------------------
         * </pre>
         * @return This
         */
        public Builder withComparativeOperators()
        {
            addComparativeOperators();
            return this;
        }
        //endregion

        //region add single operators
        /**
         * <p>Defines a prefix operator for equations made by this builder.</p>
         *
         * <p>Prefix operators are invoked by prefixing an operand (an equation) with the operator's token. e.g. "-7" is
         * 7 prefixed with the minus prefix operator.</p>
         *
         * <p>Registering an operator also registers the tokens used to invoke that operator if they're not already
         * registered. Tokens should be added in reverse order of the order they should be parsed in. That is, tokens
         * that may contain other tokens should be added after those other tokens, and where tokens overlap, the most
         * recently added token takes precedence.</p>
         *
         * <p>In short, register tokens (including by registering operators) in order from shortest to longest.</p>
         * @param token The text representation of the operator, what operands are prefixed with to invoke it.
         * @param action The implementation of the operator.
         * @return This.
         */
        public Builder withPrefixOperator(String token, UnaryOperatorAction action)
        { return withPrefixOperator(token, DEFAULT_PRIORITY, action); }

        /**
         * <p>Defines a prefix operator for equations made by this builder.</p>
         *
         * <p>Prefix operators are invoked by prefixing an operand (an equation) with the operator's token. e.g. "-7" is
         * 7 prefixed with the minus prefix operator.</p>
         *
         * <p>Registering an operator also registers the tokens used to invoke that operator if they're not already
         * registered. Tokens should be added in reverse order of the order they should be parsed in. That is, tokens
         * that may contain other tokens should be added after those other tokens, and where tokens overlap, the most
         * recently added token takes precedence.</p>
         *
         * <p>In short, register tokens (including by registering operators) in order from shortest to longest.</p>
         * @param token The text representation of the operator, what operands are prefixed with to invoke it.
         * @param priority The operator's priority. Operators of higher priority are "stickier"
         * @param action The implementation of the operator.
         * @return This.
         */
        public Builder withPrefixOperator(String token, double priority, UnaryOperatorAction action)
        {
            addOperator(new PrefixOperator(new Token(token), priority, action));
            return this;
        }

        /**
         * <p>Defines a postfix operator for equations made by this builder.</p>
         *
         * <p>Postfix operators are invoked by postfixing an operand (an equation) with the operator's token. e.g. "7%"
         * is 7 postfixed with the percentage postfix operator.</p>
         *
         * <p>Registering an operator also registers the tokens used to invoke that operator if they're not already
         * registered. Tokens should be added in reverse order of the order they should be parsed in. That is, tokens
         * that may contain other tokens should be added after those other tokens, and where tokens overlap, the most
         * recently added token takes precedence.</p>
         *
         * <p>In short, register tokens (including by registering operators) in order from shortest to longest.</p>
         * @param token The text representation of the operator, what operands are postfixed with to invoke it.
         * @param action The implementation of the operator.
         * @return This.
         */
        public Builder withPostfixOperator(String token, UnaryOperatorAction action)
        { return withPostfixOperator(token, DEFAULT_PRIORITY, action); }

        /**
         * <p>Defines a postfix operator for equations made by this builder.</p>
         *
         * <p>Postfix operators are invoked by postfixing an operand (an equation) with the operator's token. e.g. "7%"
         * is 7 postfixed with the percentage postfix operator.</p>
         *
         * <p>Registering an operator also registers the tokens used to invoke that operator if they're not already
         * registered. Tokens should be added in reverse order of the order they should be parsed in. That is, tokens
         * that may contain other tokens should be added after those other tokens, and where tokens overlap, the most
         * recently added token takes precedence.</p>
         *
         * <p>In short, register tokens (including by registering operators) in order from shortest to longest.</p>
         * @param token The text representation of the operator, what operands are postfixed with to invoke it.
         * @param priority The operator's priority. Operators of higher priority are "stickier"
         * @param action The implementation of the operator.
         * @return This.
         */
        public Builder withPostfixOperator(String token, double priority, UnaryOperatorAction action)
        {
            addOperator(new PostfixOperator(new Token(token), priority, action));
            return this;
        }

        /**
         * <p>Defines an binary infix operator for equations made by this builder.</p>
         *
         * <p>Binary operators are invoked by placing the text representation of the operator between two operands.
         * (equations) e.g. "7+5" is the addition operator ("+") being invoked, passing in 7 and 5.</p>
         *
         * <p>Registering an operator also registers the tokens used to invoke that operator if they're not already
         * registered. Tokens should be added in reverse order of the order they should be parsed in. That is, tokens
         * that may contain other tokens should be added after those other tokens, and where tokens overlap, the most
         * recently added token takes precedence.</p>
         *
         * <p>In short, register tokens (including by registering operators) in order from shortest to longest.</p>
         * @param token The text representation of the operator, what is placed between two operands to invoke it.
         * @param action The implementation of the operator.
         * @return This.
         */
        public Builder withOperator(String token, BinaryOperatorAction action)
        { return withOperator(token, DEFAULT_ASSOCIATIVITY, DEFAULT_PRIORITY, action); }

        /**
         * <p>Defines an binary infix operator for equations made by this builder.</p>
         *
         * <p>Binary operators are invoked by placing the text representation of the operator between two operands.
         * (equations) e.g. "7+5" is the addition operator ("+") being invoked, passing in 7 and 5.</p>
         *
         * <p>Registering an operator also registers the tokens used to invoke that operator if they're not already
         * registered. Tokens should be added in reverse order of the order they should be parsed in. That is, tokens
         * that may contain other tokens should be added after those other tokens, and where tokens overlap, the most
         * recently added token takes precedence.</p>
         *
         * <p>In short, register tokens (including by registering operators) in order from shortest to longest.</p>
         * @param token The text representation of the operator, what is placed between two operands to invoke it.
         * @param priority The operator's priority, how "sticky" it is.
         * @param action The implementation of the operator.
         * @return This.
         */
        public Builder withOperator(String token, double priority, BinaryOperatorAction action)
        { return withOperator(token, DEFAULT_ASSOCIATIVITY, priority, action); }

        /**
         * <p>Defines an binary infix operator for equations made by this builder.</p>
         *
         * <p>Binary operators are invoked by placing the text representation of the operator between two operands.
         * (equations) e.g. "7+5" is the addition operator ("+") being invoked, passing in 7 and 5.</p>
         *
         * <p>Registering an operator also registers the tokens used to invoke that operator if they're not already
         * registered. Tokens should be added in reverse order of the order they should be parsed in. That is, tokens
         * that may contain other tokens should be added after those other tokens, and where tokens overlap, the most
         * recently added token takes precedence.</p>
         *
         * <p>In short, register tokens (including by registering operators) in order from shortest to longest.</p>
         * @param token The text representation of the operator, what is placed between two operands to invoke it.
         * @param isLeftAssociative The operator's associativity. Where multiple infix operators are chained together
         *                          and have the same level of priority, this determines whether they're "stickier" the
         *                          further right or left they are. e.g. "5+6+7", were "+" to be right-associative,
         *                          could be written as "5+(6+7)". Infix operators are left-associative by default.
         * @param action The implementation of the operator.
         * @return This.
         */
        public Builder withOperator(String token, boolean isLeftAssociative, BinaryOperatorAction action)
        { return withOperator(token, isLeftAssociative, DEFAULT_PRIORITY, action); }

        /**
         * <p>Defines an binary infix operator for equations made by this builder.</p>
         *
         * <p>Binary operators are invoked by placing the text representation of the operator between two operands.
         * (equations) e.g. "7+5" is the addition operator ("+") being invoked, passing in 7 and 5.</p>
         *
         * <p>Registering an operator also registers the tokens used to invoke that operator if they're not already
         * registered. Tokens should be added in reverse order of the order they should be parsed in. That is, tokens
         * that may contain other tokens should be added after those other tokens, and where tokens overlap, the most
         * recently added token takes precedence.</p>
         *
         * <p>In short, register tokens (including by registering operators) in order from shortest to longest.</p>
         * @param token The text representation of the operator, what is placed between two operands to invoke it.
         * @param isLeftAssociative The operator's associativity. Where multiple infix operators are chained together
         *                          and have the same level of priority, this determines whether they're "stickier" the
         *                          further right or left they are. e.g. "5+6+7", were "+" to be right-associative,
         *                          could be written as "5+(6+7)". Infix operators are left-associative by default.
         * @param priority The operator's priority, how "sticky" it is.
         * @param action The implementation of the operator.
         * @return This.
         */
        public Builder withOperator(String token,
                                    boolean isLeftAssociative,
                                    double priority,
                                    BinaryOperatorAction action)
        {
            addOperator(new BinaryOperator(new Token(token), isLeftAssociative, priority, action));
            return this;
        }

        /**
         * <p>Defines a ternary infix operator for equations made by this builder.</p>
         * 
         * <p>Ternary operators are invoked by placing the text representations of the operator between three operands.
         * (equations) e.g. "5?6:7" is the conditional operator ("?" and ":") being invoked, passing in 5, 6, and 7.</p>
         *
         * <p>Registering an operator also registers the tokens used to invoke that operator if they're not already
         * registered. Tokens should be added in reverse order of the order they should be parsed in. That is, tokens
         * that may contain other tokens should be added after those other tokens, and where tokens overlap, the most
         * recently added token takes precedence.</p>
         *
         * <p>In short, register tokens (including by registering operators) in order from shortest to longest.</p>
         * @param token1 The left text representation of the operator, which must be placed between the first and second
         *               operand to invoke it.
         * @param token2 The right text representation of the operator, which must be placed between the second and third
         *               operand to invoke it.
         * @param action The implementation of the operator.
         * @return This.
         */
        public Builder withOperator(String token1, String token2, TernaryOperatorAction action)
        { return withOperator(token1, token2, DEFAULT_ASSOCIATIVITY, DEFAULT_PRIORITY, action); }

        /**
         * <p>Defines a ternary infix operator for equations made by this builder.</p>
         *
         * <p>Ternary operators are invoked by placing the text representations of the operator between three operands.
         * (equations) e.g. "5?6:7" is the conditional operator ("?" and ":") being invoked, passing in 5, 6, and 7.</p>
         *
         * <p>Registering an operator also registers the tokens used to invoke that operator if they're not already
         * registered. Tokens should be added in reverse order of the order they should be parsed in. That is, tokens
         * that may contain other tokens should be added after those other tokens, and where tokens overlap, the most
         * recently added token takes precedence.</p>
         *
         * <p>In short, register tokens (including by registering operators) in order from shortest to longest.</p>
         * @param token1 The left text representation of the operator, which must be placed between the first and second
         *               operand to invoke it.
         * @param token2 The right text representation of the operator, which must be placed between the second and third
         *               operand to invoke it.
         * @param priority The operator's priority, how "sticky" it is.
         * @param action The implementation of the operator.
         * @return This.
         */
        public Builder withOperator(String token1, String token2, double priority, TernaryOperatorAction action)
        { return withOperator(token1, token2, DEFAULT_ASSOCIATIVITY, priority, action); }

        /**
         * <p>Defines a ternary infix operator for equations made by this builder.</p>
         *
         * <p>Ternary operators are invoked by placing the text representations of the operator between three operands.
         * (equations) e.g. "5?6:7" is the conditional operator ("?" and ":") being invoked, passing in 5, 6, and 7.</p>
         *
         * <p>Registering an operator also registers the tokens used to invoke that operator if they're not already
         * registered. Tokens should be added in reverse order of the order they should be parsed in. That is, tokens
         * that may contain other tokens should be added after those other tokens, and where tokens overlap, the most
         * recently added token takes precedence.</p>
         *
         * <p>In short, register tokens (including by registering operators) in order from shortest to longest.</p>
         * @param token1 The left text representation of the operator, which must be placed between the first and second
         *               operand to invoke it.
         * @param token2 The right text representation of the operator, which must be placed between the second and third
         *               operand to invoke it.
         * @param isLeftAssociative The operator's associativity. Where multiple infix operators are chained together
         *                          and have the same level of priority, this determines whether they're "stickier" the
         *                          further right or left they are. e.g. "5+6+7", were "+" to be right-associative,
         *                          could be written as "5+(6+7)". Infix operators are left-associative by default.
         * @param action The implementation of the operator.
         * @return This.
         */
        public Builder withOperator(String token1, String token2, boolean isLeftAssociative, TernaryOperatorAction action)
        { return withOperator(token1, token2, isLeftAssociative, DEFAULT_PRIORITY, action); }

        /**
         * <p>Defines a ternary infix operator for equations made by this builder.</p>
         *
         * <p>Ternary operators are invoked by placing the text representations of the operator between three operands.
         * (equations) e.g. "5?6:7" is the conditional operator ("?" and ":") being invoked, passing in 5, 6, and 7.</p>
         *
         * <p>Registering an operator also registers the tokens used to invoke that operator if they're not already
         * registered. Tokens should be added in reverse order of the order they should be parsed in. That is, tokens
         * that may contain other tokens should be added after those other tokens, and where tokens overlap, the most
         * recently added token takes precedence.</p>
         *
         * <p>In short, register tokens (including by registering operators) in order from shortest to longest.</p>
         * @param token1 The left text representation of the operator, which must be placed between the first and second
         *               operand to invoke it.
         * @param token2 The right text representation of the operator, which must be placed between the second and third
         *               operand to invoke it.
         * @param isLeftAssociative The operator's associativity. Where multiple infix operators are chained together
         *                          and have the same level of priority, this determines whether they're "stickier" the
         *                          further right or left they are. e.g. "5+6+7", were "+" to be right-associative,
         *                          could be written as "5+(6+7)". Infix operators are left-associative by default.
         * @param priority The operator's priority, how "sticky" it is.
         * @param action The implementation of the operator.
         * @return This.
         */
        public Builder withOperator(String token1,
                                    String token2,
                                    boolean isLeftAssociative,
                                    double priority,
                                    TernaryOperatorAction action)
        {
            addOperator(new TernaryOperator(new Token(token1), new Token(token2), isLeftAssociative, priority, action));
            return this;
        }

        /**
         * <p>Defines an n-ary infix operator for equations made by this builder, where n is the number of strings in
         * the array of text representations passed to this operator.</p>
         *
         * <p>Infix operators are invoked by placing the text representations of the operator between operands.
         * (Equations) e.g. "5 § 6 ~ 7 @ 8" is invoking the hypothetical quaternary operator using "§", "~", and "@",
         * passing in the operands 5, 6, 7, and 7.</p>
         *
         * <p>Registering an operator also registers the tokens used to invoke that operator if they're not already
         * registered. Tokens should be added in reverse order of the order they should be parsed in. That is, tokens
         * that may contain other tokens should be added after those other tokens, and where tokens overlap, the most
         * recently added token takes precedence.</p>
         *
         * <p>In short, register tokens (including by registering operators) in order from shortest to longest.</p>
         * @param tokens An array of the text representations of this operator, which must be placed between operands
         *               (equations) in order to invoke it.
         * @param action The implementation of the operator. The operands are passed in as an array of doubles.
         * @return This.
         */
        public Builder withOperator(String[] tokens, OperatorAction action)
        { return withOperator(Arrays.asList(tokens), action); }

        /**
         * <p>Defines an n-ary infix operator for equations made by this builder, where n is the number of strings in
         * the array of text representations passed to this operator.</p>
         *
         * <p>Infix operators are invoked by placing the text representations of the operator between operands.
         * (Equations) e.g. "5 § 6 ~ 7 @ 8" is invoking the hypothetical quaternary operator using "§", "~", and "@",
         * passing in the operands 5, 6, 7, and 7.</p>
         *
         * <p>Registering an operator also registers the tokens used to invoke that operator if they're not already
         * registered. Tokens should be added in reverse order of the order they should be parsed in. That is, tokens
         * that may contain other tokens should be added after those other tokens, and where tokens overlap, the most
         * recently added token takes precedence.</p>
         *
         * <p>In short, register tokens (including by registering operators) in order from shortest to longest.</p>
         * @param tokens An array of the text representations of this operator, which must be placed between operands
         *               (equations) in order to invoke it.
         * @param isLeftAssociative The operator's associativity. Where multiple infix operators are chained together
         *                          and have the same level of priority, this determines whether they're "stickier" the
         *                          further right or left they are. e.g. "5+6+7", were "+" to be right-associative,
         *                          could be written as "5+(6+7)". Infix operators are left-associative by default.
         * @param action The implementation of the operator. The operands are passed in as an array of doubles.
         * @return This.
         */
        public Builder withOperator(String[] tokens, boolean isLeftAssociative, OperatorAction action)
        { return withOperator(Arrays.asList(tokens), isLeftAssociative, action); }

        /**
         * <p>Defines an n-ary infix operator for equations made by this builder, where n is the number of strings in
         * the array of text representations passed to this operator.</p>
         *
         * <p>Infix operators are invoked by placing the text representations of the operator between operands.
         * (Equations) e.g. "5 § 6 ~ 7 @ 8" is invoking the hypothetical quaternary operator using "§", "~", and "@",
         * passing in the operands 5, 6, 7, and 7.</p>
         *
         * <p>Registering an operator also registers the tokens used to invoke that operator if they're not already
         * registered. Tokens should be added in reverse order of the order they should be parsed in. That is, tokens
         * that may contain other tokens should be added after those other tokens, and where tokens overlap, the most
         * recently added token takes precedence.</p>
         *
         * <p>In short, register tokens (including by registering operators) in order from shortest to longest.</p>
         * @param tokens An array of the text representations of this operator, which must be placed between operands
         *               (equations) in order to invoke it.
         * @param priority The operator's priority, how "sticky" it is.
         * @param action The implementation of the operator. The operands are passed in as an array of doubles.
         * @return This.
         */
        public Builder withOperator(String[] tokens, double priority, OperatorAction action)
        { return withOperator(Arrays.asList(tokens), priority, action); }

        /**
         * <p>Defines an n-ary infix operator for equations made by this builder, where n is the number of strings in
         * the array of text representations passed to this operator.</p>
         *
         * <p>Infix operators are invoked by placing the text representations of the operator between operands.
         * (Equations) e.g. "5 § 6 ~ 7 @ 8" is invoking the hypothetical quaternary operator using "§", "~", and "@",
         * passing in the operands 5, 6, 7, and 7.</p>
         *
         * <p>Registering an operator also registers the tokens used to invoke that operator if they're not already
         * registered. Tokens should be added in reverse order of the order they should be parsed in. That is, tokens
         * that may contain other tokens should be added after those other tokens, and where tokens overlap, the most
         * recently added token takes precedence.</p>
         *
         * <p>In short, register tokens (including by registering operators) in order from shortest to longest.</p>
         * @param tokens An array of the text representations of this operator, which must be placed between operands
         *               (equations) in order to invoke it.
         * @param isLeftAssociative The operator's associativity. Where multiple infix operators are chained together
         *                          and have the same level of priority, this determines whether they're "stickier" the
         *                          further right or left they are. e.g. "5+6+7", were "+" to be right-associative,
         *                          could be written as "5+(6+7)". Infix operators are left-associative by default.
         * @param priority The operator's priority, how "sticky" it is.
         * @param action The implementation of the operator. The operands are passed in as an array of doubles.
         * @return This.
         */
        public Builder withOperator(String[] tokens, boolean isLeftAssociative, double priority, OperatorAction action)
        { return withOperator(Arrays.asList(tokens), isLeftAssociative, priority, action); }

        /**
         * <p>Defines an n-ary infix operator for equations made by this builder, where n is the number of strings in
         * the list of text representations passed to this operator.</p>
         *
         * <p>Infix operators are invoked by placing the text representations of the operator between operands.
         * (Equations) e.g. "5 § 6 ~ 7 @ 8" is invoking the hypothetical quaternary operator using "§", "~", and "@",
         * passing in the operands 5, 6, 7, and 7.</p>
         *
         * <p>Registering an operator also registers the tokens used to invoke that operator if they're not already
         * registered. Tokens should be added in reverse order of the order they should be parsed in. That is, tokens
         * that may contain other tokens should be added after those other tokens, and where tokens overlap, the most
         * recently added token takes precedence.</p>
         *
         * <p>In short, register tokens (including by registering operators) in order from shortest to longest.</p>
         * @param tokens A list of the text representations of this operator, which must be placed between operands
         *               (equations) in order to invoke it.
         * @param action The implementation of the operator. The operands are passed in as an array of doubles.
         * @return This.
         */
        public Builder withOperator(List<String> tokens, OperatorAction action)
        { return withOperator(tokens, DEFAULT_ASSOCIATIVITY, DEFAULT_PRIORITY, action); }

        /**
         * <p>Defines an n-ary infix operator for equations made by this builder, where n is the number of strings in
         * the list of text representations passed to this operator.</p>
         *
         * <p>Infix operators are invoked by placing the text representations of the operator between operands.
         * (Equations) e.g. "5 § 6 ~ 7 @ 8" is invoking the hypothetical quaternary operator using "§", "~", and "@",
         * passing in the operands 5, 6, 7, and 7.</p>
         *
         * <p>Registering an operator also registers the tokens used to invoke that operator if they're not already
         * registered. Tokens should be added in reverse order of the order they should be parsed in. That is, tokens
         * that may contain other tokens should be added after those other tokens, and where tokens overlap, the most
         * recently added token takes precedence.</p>
         *
         * <p>In short, register tokens (including by registering operators) in order from shortest to longest.</p>
         * @param tokens A list of the text representations of this operator, which must be placed between operands
         *               (equations) in order to invoke it.
         * @param isLeftAssociative The operator's associativity. Where multiple infix operators are chained together
         *                          and have the same level of priority, this determines whether they're "stickier" the
         *                          further right or left they are. e.g. "5+6+7", were "+" to be right-associative,
         *                          could be written as "5+(6+7)". Infix operators are left-associative by default.
         * @param action The implementation of the operator. The operands are passed in as an array of doubles.
         * @return This.
         */
        public Builder withOperator(List<String> tokens, boolean isLeftAssociative, OperatorAction action)
        { return withOperator(tokens, isLeftAssociative, DEFAULT_PRIORITY, action); }

        /**
         * <p>Defines an n-ary infix operator for equations made by this builder, where n is the number of strings in
         * the list of text representations passed to this operator.</p>
         *
         * <p>Infix operators are invoked by placing the text representations of the operator between operands.
         * (Equations) e.g. "5 § 6 ~ 7 @ 8" is invoking the hypothetical quaternary operator using "§", "~", and "@",
         * passing in the operands 5, 6, 7, and 7.</p>
         *
         * <p>Registering an operator also registers the tokens used to invoke that operator if they're not already
         * registered. Tokens should be added in reverse order of the order they should be parsed in. That is, tokens
         * that may contain other tokens should be added after those other tokens, and where tokens overlap, the most
         * recently added token takes precedence.</p>
         *
         * <p>In short, register tokens (including by registering operators) in order from shortest to longest.</p>
         * @param tokens A list of the text representations of this operator, which must be placed between operands
         *               (equations) in order to invoke it.
         * @param priority The operator's priority, how "sticky" it is.
         * @param action The implementation of the operator. The operands are passed in as an array of doubles.
         * @return This.
         */
        public Builder withOperator(List<String> tokens, double priority, OperatorAction action)
        { return withOperator(tokens, DEFAULT_ASSOCIATIVITY, priority, action); }

        /**
         * <p>Defines an n-ary infix operator for equations made by this builder, where n is the number of strings in
         * the list of text representations passed to this operator.</p>
         *
         * <p>Infix operators are invoked by placing the text representations of the operator between operands.
         * (Equations) e.g. "5 § 6 ~ 7 @ 8" is invoking the hypothetical quaternary operator using "§", "~", and "@",
         * passing in the operands 5, 6, 7, and 7.</p>
         *
         * <p>Registering an operator also registers the tokens used to invoke that operator if they're not already
         * registered. Tokens should be added in reverse order of the order they should be parsed in. That is, tokens
         * that may contain other tokens should be added after those other tokens, and where tokens overlap, the most
         * recently added token takes precedence.</p>
         *
         * <p>In short, register tokens (including by registering operators) in order from shortest to longest.</p>
         * @param tokens A list of the text representations of this operator, which must be placed between operands
         *               (equations) in order to invoke it.
         * @param isLeftAssociative The operator's associativity. Where multiple infix operators are chained together
         *                          and have the same level of priority, this determines whether they're "stickier" the
         *                          further right or left they are. e.g. "5+6+7", were "+" to be right-associative,
         *                          could be written as "5+(6+7)". Infix operators are left-associative by default.
         * @param priority The operator's priority, how "sticky" it is.
         * @param action The implementation of the operator. The operands are passed in as an array of doubles.
         * @return This.
         */
        public Builder withOperator(List<String> tokens,
                                    boolean isLeftAssociative,
                                    double priority,
                                    OperatorAction action)
        {
            List<Token> ts = new ArrayList<>(tokens.size());

            for(String i : tokens)
                ts.add(new Token(i));

            addOperator(new InfixOperator(ts, isLeftAssociative, priority, action));
            return this;
        }
        //endregion
        //endregion
        //endregion
        //endregion

        //region parsing

        /**
         * Builds the given equation as a string, into an {@link Equation} object, which may then be evaluated.
         * @param toParse The equation as a string.
         * @return The equation representation of the given string.
         * @throws EquationParseException is the given string is not a valid equation.
         */
        public Equation build(String toParse)
        {
            if(toParse == null)
                throw new IllegalArgumentException("Cannot parse a null string as an equation.");

            if(toParse.isEmpty())
                throw new IllegalArgumentException("Cannot parse an empty string as an equation.");

            buildOperatorGroups();
            List<Token> possibleTokensInReverseOrder = new ArrayList<>(this.possibleTokensInOrder);
            Collections.reverse(possibleTokensInReverseOrder);
            Tokeniser tokeniser = new Tokeniser(possibleTokensInReverseOrder);
            TokenList tokenisation = tokeniser.tokenise(toParse).unmodifiable();
            verifyTokenisationBrackets(tokenisation);
            EquationComponent topLevelComponent;

            try
            { topLevelComponent = tryParse(tokenisation); }
            catch(EquationParseException e)
            { throw e.withFullEquation(tokenisation); }

            return new Equation(topLevelComponent, new HashMap<>(variables), new HashMap<>(functions));
        }

        /**
         * Compiles this builder's operator priority groups from its operators.
         */
        void buildOperatorGroups()
        {
            if(operatorGroups != null)
                return;

            operatorGroups = new HashMap<>();

            for(Map.Entry<Token, PrefixOperator> e : prefixOperators.entrySet())
            {
                operatorGroups.computeIfAbsent(e.getValue().priority, x -> new OperatorPriorityGroup())
                        .prefixOperators
                        .put(e.getKey(), e.getValue());
            }

            for(Map.Entry<Token, PostfixOperator> e : postfixOperators.entrySet())
            {
                operatorGroups.computeIfAbsent(e.getValue().priority, x -> new OperatorPriorityGroup())
                        .postfixOperators
                        .put(e.getKey(), e.getValue());
            }

            for(Map.Entry<List<Token>, InfixOperator> e : infixOperators.entrySet())
            {
                if(e.getValue().isLeftAssociative)
                    operatorGroups.computeIfAbsent(e.getValue().priority, x -> new OperatorPriorityGroup())
                            .leftAssociativeInfixOperators
                            .setAt(e.getValue(), e.getValue().tokens);
                else
                    operatorGroups.computeIfAbsent(e.getValue().priority, x -> new OperatorPriorityGroup())
                            .rightAssociativeInfixOperators
                            .setAt(e.getValue(), e.getValue().tokens);
            }

            operatorGroupsInOrder = operatorGroups.entrySet()
                                                  .stream()
                                                  .sorted(Map.Entry.comparingByKey())
                                                  .map(Map.Entry::getValue)
                                                  .collect(Collectors.toList());
        }

        /**
         * Invalidates this builder's currently built priority groups. This should be done when the builder is updated
         * in a way that renders the previously built priority groups wrong, such as a new operator being added.
         */
        void invalidateOperatorGroups()
        {
            operatorGroups = null;
            operatorGroupsInOrder = null;
        }

        /**
         * Ensures that the given tokenlist contains no unmatched brackets.
         * @param tokenisation The tokenlist to check.
         */
        void verifyTokenisationBrackets(TokenList tokenisation)
        {
            int bracketDepth = 0;

            for(Token t : tokenisation.tokens)
            {
                if(t.equals(Token.OPEN_BRACKET))
                    bracketDepth++;
                else if(t.equals(Token.CLOSE_BRACKET))
                {
                    if(--bracketDepth < 0)
                        throw new UnexpectedCloseBracketException(tokenisation);
                }
            }

            if(bracketDepth > 0)
                throw new UnmatchedOpenBracketException(tokenisation);
        }

        /**
         * Attempts to parse a tokenlist into an equation component.
         * @param tokenisation The tokenlist to parse.
         * @return The given tokenlist, compiled into an equation component.
         * @throws EquationParseException if the given tokenlist is not a valid equation.
         */
        EquationComponent tryParse(TokenList tokenisation)
        {
            if(startsWithNonPrefixOperator(tokenisation))
                throw new LeadingNonPrefixOperatorException(tokenisation, tokenisation);

            if(endsWithNonPostfixOperator(tokenisation))
                throw new TrailingNonPostfixOperatorException(tokenisation, tokenisation);

            if(tokenisation.isInBrackets())
                return tryParse(tokenisation.withoutFirstAndLast());

            return nullCoalesce(() -> tryParseVariable(tokenisation),
                                () -> tryParseFunctionCall(tokenisation),
                                () -> tryParseOperation(tokenisation),
                                () -> tryParseNumber(tokenisation),
                                () -> { throw new EquationParseException(tokenisation, tokenisation); });
        }

        //region parsing utility functions

        /**
         * Checks if a given tokenlist starts with an operator that may not be a prefix operator.
         * @param tokenList The tokenlist to check.
         * @return True if the tokenlist starts with an operator that may not be a prefix operator. Otherwise, false.
         */
        boolean startsWithNonPrefixOperator(TokenList tokenList)
        {
            Token first = tokenList.first();
            return operatorTokens.contains(first) && !prefixOperators.containsKey(first);
        }

        /**
         * Checks if a given tokenlist ends with an operator that may not be a postfix operator.
         * @param tokenList The tokenlist to check.
         * @return True if the tokenlist ends with an operator that may not be a postfix operator. Otherwise, false.
         */
        boolean endsWithNonPostfixOperator(TokenList tokenList)
        {
            Token last = tokenList.last();
            return operatorTokens.contains(last) && !postfixOperators.containsKey(last);
        }

        // Tokens in a tokenised equation may only be infix operators where:
        //  - There are tokens before and after the run.
        //  - All tokens in the run of operator tokens it's in before it may be postfix operators
        //  - All tokens in the run of operator tokens it's in after it may be prefix operators

        /**
         * <p>Checks whether or not the token at a given index in a given tokenlist can be an infix operator, considering
         * the state of the cluster of operator tokens the token at the given index is in.</p>
         *
         * <p>Tokens are considered to possibly be an infix operator where all of the following are true:</p>
         * <ul>
         *     <li>There are tokens before and after the cluster of operator tokens it's in.</li>
         *     <li>All tokens in the cluster before the index may be postfix operators.</li>
         *     <li>All tokens in the cluster after the index may be prefix operators.</li>
         * </ul>
         * @param tokens The tokenlist to check in.
         * @param tokenIndex The index of the token in the given tokenlist to check.
         * @return True if the token at the given index in the tokenlist can theoretically be an infix operator.
         * Otherwise, false.
         */
        boolean canBeInfixOperatorToken(List<Token> tokens, int tokenIndex)
        {
            if(tokenIndex == 0 || tokenIndex == tokens.size() - 1)
                return false;

            OperatorTokenRun run = getOpRun(tokens, tokenIndex);

            if((run.startIndexInSource == 0) || (run.endIndexInSource == tokens.size() - 1))
                return false;

            for(Token i : run.tokensBeforePivot)
                if(!postfixOperators.containsKey(i))
                    return false;

            for(Token i : run.tokensAfterPivot)
                if(!prefixOperators.containsKey(i))
                    return false;

            return true;
        }

        // idk, I'm bad at naming things.

        /**
         * Gets the cluster of operator tokens the token at the given index is in in the given token list.
         * @param tokens The token list to check in.
         * @param indexToGetOpRunThatContainsIt The index of the token.
         * @return An OperatorTokenRun object, instantiated with the appropriate information.
         */
        OperatorTokenRun getOpRun(List<Token> tokens, int indexToGetOpRunThatContainsIt)
        {
            int min = -1, max = -1;

            for(int i = indexToGetOpRunThatContainsIt - 1; i >= 0; i--)
            {
                Token itoken = tokens.get(i);

                if(!operatorTokens.contains(itoken))
                {
                    min = i + 1;
                    break;
                }
            }

            for(int i = indexToGetOpRunThatContainsIt + 1; i < tokens.size(); i++)
            {
                Token itoken = tokens.get(i);

                if(!operatorTokens.contains(itoken))
                {
                    max = i - 1;
                    break;
                }
            }

            if(min == -1)
                min = 0;

            if(max == -1)
                max = tokens.size() - 1;

            return new OperatorTokenRun(tokens, min, max, indexToGetOpRunThatContainsIt);
        }
        //endregion

        //region variable parsing

        /**
         * Checks to see if the given tokenlist matches an available variable.
         * @param tokenList The tokenlist to check.
         * @return If the given tokenlist does not match a variable, null. Otherwise, a reference to that variable.
         */
        VariableReference tryParseVariable(TokenList tokenList)
        {
            String varName = tokenList.equationAsString.trim();
            Double variableValue = variables.get(varName);
            return variableValue == null ? null : new VariableReference(varName);
        }
        //endregion

        //region function parsing

        /**
         * Checks to see if the given tokenlist is identifiable as a function and matches an available one.
         * @param tokenList The tokenlist to check.
         * @return Null if the given tokenlist does not match a function. Otherwise, a reference to that function and
         * the arguments passed into it.
         * @throws UnrecognisedFunctionException Where the given tokenlist can only be a function call, but there is
         *                                       no function by the given name available.
         */
        FunctionCall tryParseFunctionCall(TokenList tokenList)
        {
            if(tokenList.size() < 3) // Needs at least 3 tokens: a name, "(", and ")".
                return null;

            if(!tokenList.last().equals(Token.CLOSE_BRACKET))
                return null;

            int lastIndexToCheck = tokenList.size() - 2;
            String functionName = null;
            TokenList argListTokenList = null;

            for(int i = 1; i <= lastIndexToCheck; i++)
            {
                if(tokenList.get(i).equals(Token.OPEN_BRACKET))
                {
                    functionName = tokenList.subList(0, i).equationAsString.trim();
                    argListTokenList = tokenList.subList(i + 1, tokenList.size() - 1);
                    break;
                }
            }

            if(argListTokenList == null)
                return null;

            EquationComponent[] arguments;

            if(argListTokenList.isEmpty())
                arguments = new EquationComponent[0];
            else
            {
                if(argListTokenList.startsWith(Token.ARGUMENT_SEPARATOR))
                    throw new LeadingArgumentSeparatorException(tokenList, tokenList);

                if(argListTokenList.endsWith(Token.ARGUMENT_SEPARATOR))
                    throw new TrailingArgumentSeparatorException(tokenList, tokenList);

                List<TokenList> argTokenLists = argListTokenList.splitBy(Token.ARGUMENT_SEPARATOR);
                arguments = new EquationComponent[argTokenLists.size()];

                for(int i = 0; i < argTokenLists.size(); i++)
                {
                    TokenList argTokenList = argTokenLists.get(i);

                    if(argTokenList.isEmpty())
                        throw new EmptyFunctionArgumentException(tokenList, tokenList);

                    arguments[i] = tryParse(argTokenList);
                }
            }

            if(!functions.containsKey(functionName))
            {
                if(tokenList.containsAnyOf(operatorTokens))
                    return null;

                throw new UnrecognisedFunctionException(functionName, tokenList, tokenList);
            }

            return new FunctionCall(functionName, arguments);
        }
        //endregion

        //region operation parsing

        /**
         * Attempts to parse the given tokenlist as an operator.
         * @param tokenList The tokenlist to check.
         * @return Null if the given tokenlist is not an operation. Otherwise, an operator object, tying the operator
         *         being invoked with the operands passed into it.
         */
        Operation tryParseOperation(TokenList tokenList)
        {
            /*

            Notes:
             - Where left- and right-associative operators share the same priority, right associative operators are
               tested first. (and therefore left associative operators are more "sticky")
             - The order of infix operators of the same associativity and priority, is determined by the order in which
               their tokens appear, from left-to-right for right-associative operators and vice versa for
               left-associative.
             */

            for(int i = 0; i < operatorGroupsInOrder.size(); i++)
            {
                Operation o = tryParseOperation(tokenList, operatorGroupsInOrder.get(i), i);

                if(o != null)
                    return o;
            }

            return null;
        }

        /**
         * Attempts to parse a given tokenlist as an operator, in the context of an operator group.
         * @param tokenList The tokenlist to check.
         * @param opGroup The operator group containing the operators to be checked for.
         * @param opGroupIndex The index of the operator group in the ordered operator groups.
         * @return Null if the given tokenlist is not an operation available within the given operator group. Otherwise,
         *         an operator object, tying the operator being invoked with the operands passed into it.
         */
        Operation tryParseOperation(TokenList tokenList, OperatorPriorityGroup opGroup, int opGroupIndex)
        {
            return nullCoalesce(() -> tryParseInfixOperation_rightAssociative(tokenList, opGroup, opGroupIndex),
                                () -> tryParseInfixOperation_leftAssociative(tokenList, opGroup, opGroupIndex),
                                () -> tryParsePrefixOperation(tokenList, opGroup),
                                () -> tryParsePostfixOperation(tokenList, opGroup),
                                () -> null);
        }

        /**
         * Attempts to parse a given tokenlist as right-associative infix operator, in the context of an operator group.
         * @param tokenList The tokenlist to check.
         * @param opGroup The operator group containing the operators to be checked for.
         * @param opGroupIndex The index of the operator group in the ordered operator groups.
         * @return Null if the given tokenlist is not a right-assocative operation available within the given operator,
         *         group. Otherwise, an operator object, tying the operator being invoked with the operands passed into
         *         it.
         */
        Operation tryParseInfixOperation_rightAssociative(TokenList tokenList,
                                                          OperatorPriorityGroup opGroup,
                                                          int opGroupIndex)
        {
            Tree<Token, InfixOperator> opTree = opGroup.rightAssociativeInfixOperators;

            if(opTree.isEmpty())
                return null;

            List<Integer> opTokenPoints = getInfixTokenPoints_rightAssociative(tokenList, opTree, 0);

            if(opTokenPoints == null)
                return null;

            if(operationIsNestedInHigherPriorityInfixOperation(tokenList, opTokenPoints, opGroupIndex, true))
                return null;

            List<Token> opTokens = opTokenPoints.stream().map(tokenList::get).collect(Collectors.toList());
            InfixOperator op = opTree.getAt(opTokens);
            return op.tryParseFromSplits(tokenList, opTokenPoints, this);
        }

        List<Integer> getInfixTokenPoints_rightAssociative(TokenList of,
                                                           Tree<Token, InfixOperator> opsTree,
                                                           int startAt)
        { return getInfixTokenPoints_rightAssociative(of, opsTree, startAt, 0, 0); }

        List<Integer> getInfixTokenPoints_rightAssociative(TokenList of,
                                                           Tree<Token, InfixOperator> opsTree,
                                                           int startAt,
                                                           int skipFromInclusive,
                                                           int skipToExclusive)
        {
            Tree<Token, InfixOperator> opsBranch = opsTree;
            int bracketDepth = 0;
            List<Integer> points = new ArrayList<>();

            for(int i = startAt; i < of.size(); i++)
            {
                if(i >= skipFromInclusive && i < skipToExclusive)
                    continue;

                Token itoken = of.get(i);

                if(itoken.equals(Token.OPEN_BRACKET))
                    bracketDepth++;
                else if(itoken.equals(Token.CLOSE_BRACKET))
                    bracketDepth--;
                else if(bracketDepth == 0
                     && infixOperatorTokens.contains(itoken)
                     && canBeInfixOperatorToken(of.tokens, i))
                {
                    if(opsBranch.hasItemsAtOrUnder(itoken))
                    {
                        points.add(i);
                        opsBranch = opsBranch.getBranch(itoken);

                        if(opsBranch.hasRootItem())
                        {
                            List<Integer> restOfPoints = getInfixTokenPoints_rightAssociative(
                                    of, opsBranch, i + 1, skipFromInclusive, skipToExclusive);

                            if(restOfPoints != null)
                                points.addAll(restOfPoints);

                            return points;
                        }
                    }
                    else if(opsTree.hasItemsAtOrUnder(itoken))
                    {
                        List<Integer> subOfPoints = getInfixTokenPoints_rightAssociative(
                                of, opsTree, i, skipFromInclusive, skipToExclusive);

                        if(subOfPoints != null)
                            i = subOfPoints.get(subOfPoints.size() - 1);
                    }
                }
            }

            return null;
        }

        /**
         * Attempts to parse a given tokenlist as left-associative infix operator, in the context of an operator group.
         * @param tokenList The tokenlist to check.
         * @param opGroup The operator group containing the operators to be checked for.
         * @param opGroupIndex The index of the operator group in the ordered operator groups.
         * @return Null if the given tokenlist is not a left-assocative operation available within the given operator,
         *         group. Otherwise, an operator object, tying the operator being invoked with the operands passed into
         *         it.
         */
        Operation tryParseInfixOperation_leftAssociative(TokenList tokenList,
                                                         OperatorPriorityGroup opGroup,
                                                         int opGroupIndex)
        {
            Tree<Token, InfixOperator> opTree = opGroup.leftAssociativeInfixOperators;

            if(opTree.isEmpty())
                return null;

            List<Integer> opTokenPoints
                    = getInfixTokenPoints_leftAssociative(tokenList, opTree.withReversedKeys(), tokenList.size() - 1);

            if(opTokenPoints == null)
                return null;

            if(operationIsNestedInHigherPriorityInfixOperation(tokenList, opTokenPoints, opGroupIndex, false))
                return null;

            List<Token> opTokens = opTokenPoints.stream().map(tokenList::get).collect(Collectors.toList());
            InfixOperator op = opTree.getAt(opTokens);
            return op.tryParseFromSplits(tokenList, opTokenPoints, this);
        }

        List<Integer> getInfixTokenPoints_leftAssociative(TokenList of,
                                                          Tree<Token, InfixOperator> opsTreeReversed,
                                                          int startAt)
        { return getInfixTokenPoints_leftAssociative(of, opsTreeReversed, startAt, 0, 0); }

        List<Integer> getInfixTokenPoints_leftAssociative(TokenList of,
                                                          Tree<Token, InfixOperator> opsTreeReversed,
                                                          int startAt,
                                                          int skipFromInclusive,
                                                          int skipToExclusive)
        {
            Tree<Token, InfixOperator> opsBranch = opsTreeReversed;
            int bracketDepth = 0;
            List<Integer> points = new ArrayList<>();

            for(int i = startAt; i >= 0; i--)
            {
                if(i >= skipFromInclusive && i < skipToExclusive)
                    continue;

                Token itoken = of.get(i);

                if(itoken.equals(Token.CLOSE_BRACKET))
                    bracketDepth++;
                else if(itoken.equals(Token.OPEN_BRACKET))
                    bracketDepth--;
                else if(bracketDepth == 0
                     && infixOperatorTokens.contains(itoken)
                     && canBeInfixOperatorToken(of.tokens, i))
                {
                    if(opsBranch.hasItemsAtOrUnder(itoken))
                    {
                        points.add(0, i);
                        opsBranch = opsBranch.getBranch(itoken);

                        if(opsBranch.hasRootItem())
                        {
                            List<Integer> restOfPoints = getInfixTokenPoints_leftAssociative(
                                    of, opsBranch, i - 1, skipFromInclusive, skipToExclusive);

                            if(restOfPoints != null)
                                points.addAll(0, restOfPoints);

                            return points;
                        }
                    }
                    else if(opsTreeReversed.hasItemsAtOrUnder(itoken))
                    {
                        List<Integer> subOfPoints = getInfixTokenPoints_leftAssociative(
                                of, opsTreeReversed, i, skipFromInclusive, skipToExclusive);

                        if(subOfPoints != null)
                            i = subOfPoints.get(0);
                    }
                }
            }

            return null;
        }

        /**
         * Checks whether or not an operation is nested within another of higher priority.
         * @param tokenList The tokenlist to check in.
         * @param potentiallyInnerOpTokenPoints The list of indices of the identified operator tokens within the given
         *                                      token list.
         * @param opPriorityGroupIndex The index of the operator's operator group within the ordered list of operator
         *                             groups.
         * @param checkLeftAssociativeOfSameGroup Whether or not to check the left associative operators of the same
         *                                        operator group. This should generally be true for right-associative
         *                                        operators, and false for left-associative operators.
         * @return True if the identified operator is nested within another, higher priority operator. Otherwise, false.
         */
        boolean operationIsNestedInHigherPriorityInfixOperation(TokenList tokenList,
                                                                List<Integer> potentiallyInnerOpTokenPoints,
                                                                int opPriorityGroupIndex,
                                                                boolean checkLeftAssociativeOfSameGroup)
        {
            int firstOpTokenPoint = potentiallyInnerOpTokenPoints.get(0);
            int lastOpTokenPoint = potentiallyInnerOpTokenPoints.get(potentiallyInnerOpTokenPoints.size() - 1);

            int skipFromInclusive = getOpRun(tokenList.tokens, firstOpTokenPoint).startIndexInSource;
            int skipToExclusive   = getOpRun(tokenList.tokens, lastOpTokenPoint ).endIndexInSource + 1;

            // Don't need to check the same associativity of the same priority group, as in a nested arrangement of
            // infix operators of the same associativity and priority group, the outer infix operator will always be
            // parsed before the inner infix operator.

            if(checkLeftAssociativeOfSameGroup)
            {
                OperatorPriorityGroup opGroup = operatorGroupsInOrder.get(opPriorityGroupIndex);

                List<Integer> possiblyEnclosingInfixOpPoints = getInfixTokenPoints_leftAssociative(
                        tokenList,
                        opGroup.leftAssociativeInfixOperators.withReversedKeys(),
                        tokenList.size() - 1,
                        skipFromInclusive,
                        skipToExclusive);

                if(possiblyEnclosingInfixOpPoints != null
                   && possiblyEnclosingInfixOpPoints.get(0) < firstOpTokenPoint
                   && possiblyEnclosingInfixOpPoints.get(possiblyEnclosingInfixOpPoints.size() - 1) > lastOpTokenPoint)
                { return true; }
            }

            for(int i = opPriorityGroupIndex + 1; i < operatorGroupsInOrder.size(); i++)
            {
                OperatorPriorityGroup opGroup = operatorGroupsInOrder.get(i);

                List<Integer> possiblyEnclosingInfixOpPoints = getInfixTokenPoints_rightAssociative(
                        tokenList,
                        opGroup.rightAssociativeInfixOperators,
                        0,
                        skipFromInclusive,
                        skipToExclusive);

                if(possiblyEnclosingInfixOpPoints != null
                   && possiblyEnclosingInfixOpPoints.get(0) < firstOpTokenPoint
                   && possiblyEnclosingInfixOpPoints.get(possiblyEnclosingInfixOpPoints.size() - 1) > lastOpTokenPoint)
                { return true; }


                possiblyEnclosingInfixOpPoints = getInfixTokenPoints_leftAssociative(
                        tokenList,
                        opGroup.leftAssociativeInfixOperators.withReversedKeys(),
                        tokenList.size() - 1,
                        skipFromInclusive,
                        skipToExclusive);

                if(possiblyEnclosingInfixOpPoints != null
                   && possiblyEnclosingInfixOpPoints.get(0) < firstOpTokenPoint
                   && possiblyEnclosingInfixOpPoints.get(possiblyEnclosingInfixOpPoints.size() - 1) > lastOpTokenPoint)
                { return true; }
            }

            return false;
        }

        /**
         * Attempts to parse a tokenlist as a prefix operation, within the context of an operator group.
         * @param tokenList The tokenlist to check.
         * @param opGroup The The operator group containing the prefix operations to check for.
         * @return If the given tokenlist is not a prefix operation, returns null. Otherwise, returns an operation
         * object tying the operator to the operand.
         */
        Operation tryParsePrefixOperation(TokenList tokenList, OperatorPriorityGroup opGroup)
        {
            PrefixOperator op = opGroup.prefixOperators.get(tokenList.first());

            if(op == null)
                return null;

            return op.tryParse(tokenList, this);
        }

        /**
         * Attempts to parse a tokenlist as a postfix operation, within the context of an operator group.
         * @param tokenList The tokenlist to check.
         * @param opGroup The The operator group containing the postfix operations to check for.
         * @return If the given tokenlist is not a postfix operation, returns null. Otherwise, returns an operation
         * object tying the operator to the operand.
         */
        Operation tryParsePostfixOperation(TokenList tokenList, OperatorPriorityGroup opGroup)
        {
            PostfixOperator op = opGroup.postfixOperators.get(tokenList.last());

            if(op == null)
                return null;

            return op.tryParse(tokenList, this);
        }
        //endregion

        //region number parsing

        /**
         * Attempts to parse the given tokenlist as a number.
         * @param tokenList The tokenlist to check.
         * @return Null if the given tokenlist is not parseable as a number. Otherwise, a LiteralNumber, containing the
         *         number it was parsed as.
         */
        LiteralNumber tryParseNumber(TokenList tokenList)
        {
            try
            {
                double literalNumberValue = Double.parseDouble(tokenList.equationAsString.trim());
                return new LiteralNumber(literalNumberValue);
            }
            catch(NumberFormatException e)
            { return null; }
        }
        //endregion
        //endregion
        //endregion
    }

    /**
     * <p>Class for converting a string into a list of {@link Token tokens}.</p>
     *
     * <p>This works by going through the registered tokens and identifying them in the provided string, splitting it
     * into the text around and between the tokens, (including the tokens themselves in the results) and doing the same
     * to the split off parts. (That aren't tokens themselves)</p>
     */
    static class Tokeniser
    {
        /**
         * The defined tokens this tokeniser should look for, in the order it should look for them in.
         */
        List<Token> tokens;

        /**
         * Creates a new tokeniser, considering the given tokens.
         * @param tokens The tokens for the tokeniser to consider.
         */
        public Tokeniser(List<Token> tokens)
        { this.tokens = tokens; }

        /**
         * Converts a string into a token list, considering the tokens this tokeniser was initialised with.
         * @param s The string to tokenise.
         * @return A list of tokens and text before, between, and after tokens (as instances of
         *         {@link UntokenisedString}) in the passed string.
         */
        public TokenList tokenise(String s)
        {
            String sTrimmed = s.trim();

            if(sTrimmed.isEmpty())
                return new TokenList(s, Collections.emptyList(), Collections.singletonList(s.length()));

            LinkedList<Token> result = new LinkedList<>();
            LinkedList<Integer> spacesBeforeTokens = new LinkedList<>();

            spacesBeforeTokens.add(countSpacesAtStart(s));
            result.add(new UntokenisedString(sTrimmed));
            spacesBeforeTokens.add(countSpacesAtEnd(s));

            for(Token tokenToSplitOn : tokens)
            {
                ListIterator<Token> resultIterator = result.listIterator();
                ListIterator<Integer> spacingsIterator = spacesBeforeTokens.listIterator();

                while(resultIterator.hasNext())
                {
                    Token tokenToSplit = resultIterator.next();
                    spacingsIterator.next();

                    if(!(tokenToSplit instanceof UntokenisedString))
                        continue;

                    TokenList tokenSplit = tokeniseStringWithSingleToken(tokenToSplit.text, tokenToSplitOn);

                    resultIterator.remove();

                    for(Token subtoken : tokenSplit.tokens)
                        resultIterator.add(subtoken);

                    // First and last spacing should be ignored; should be 0 on both counts, as the spaces have
                    // already been accounted for and are in spacesBeforeTokens.
                    int lastSpacingIndexToAdd = tokenSplit.spacings.size() - 2;

                    for(int i = 1; i <= lastSpacingIndexToAdd; i++)
                        spacingsIterator.add(tokenSplit.spacings.get(i));
                }
            }

            tokeniseNumbers(result);
            return new TokenList(s, result, spacesBeforeTokens);
        }

        /**
         * Converts a string into a token list, considering only a single token.
         * @param tokenText The string to tokenise.
         * @param tokenToSplitBy The token to consider.
         * @return A list of instances of the given token and text before, between, and after instances of the given
         *         token (as instance of {@link UntokenisedString}) in the passed string.
         */
        static TokenList tokeniseStringWithSingleToken(String tokenText, Token tokenToSplitBy)
        {
            String textToSplitBy = tokenToSplitBy.text;
            LinkedList<Token> resultTokens = new LinkedList<>();
            LinkedList<Integer> resultSpacings = new LinkedList<>();

            int indexOfSplitter = tokenText.indexOf(textToSplitBy);
            int indexOfNextPortion = 0;

            while(indexOfSplitter != -1)
            {
                String nonSplitterPortion = tokenText.substring(indexOfNextPortion, indexOfSplitter);
                String nonSplitterPortionTrimmed = nonSplitterPortion.trim();

                if(nonSplitterPortionTrimmed.isEmpty())
                    resultSpacings.add(nonSplitterPortion.length());
                else
                {
                    resultSpacings.add(countSpacesAtStart(nonSplitterPortion));
                    resultTokens.add(new UntokenisedString(nonSplitterPortionTrimmed));
                    resultSpacings.add(countSpacesAtEnd(nonSplitterPortion));
                }

                resultTokens.add(tokenToSplitBy);

                indexOfNextPortion = indexOfSplitter + textToSplitBy.length();
                indexOfSplitter = tokenText.indexOf(textToSplitBy, indexOfNextPortion);
            }

            String finalNonSplitterPortion = tokenText.substring(indexOfNextPortion);
            String finalNonSplitterPortionTrimmed = finalNonSplitterPortion.trim();

            if(finalNonSplitterPortionTrimmed.isEmpty())
                resultSpacings.add(finalNonSplitterPortion.length());
            else
            {
                resultSpacings.add(countSpacesAtStart(finalNonSplitterPortion));
                resultTokens.add(new UntokenisedString(finalNonSplitterPortionTrimmed));
                resultSpacings.add(countSpacesAtEnd(finalNonSplitterPortion));
            }

            return new TokenList(tokenText, resultTokens, resultSpacings);
        }

        /**
         * Replaces all {@link UntokenisedString untokenised strings} in a list of tokens that are parseable as numbers
         * with {@link NumberToken number tokens}.
         * @param tokens The list of tokens to parse the numbers in.
         */
        static void tokeniseNumbers(LinkedList<Token> tokens)
        {
            ListIterator<Token> tokenIterator = tokens.listIterator();

            while(tokenIterator.hasNext())
            {
                Token token = tokenIterator.next();

                if(!(token instanceof UntokenisedString))
                    continue;

                String tokenText = token.toString();
                double asNumber;

                try
                { asNumber = Double.parseDouble(tokenText); }
                catch(NumberFormatException e)
                { continue; }

                tokenIterator.set(new NumberToken(tokenText, asNumber));
            }
        }

        /**
         * Gets the number of spaces at the start of a string.
         * @param s The string to count the leading spaces of.
         * @return The number of spaces at the start of the given string.
         */
        static int countSpacesAtStart(@NotNull String s)
        {
            for(int i = 0; i < s.length(); i++)
                if(s.charAt(i) != ' ')
                    return i;

            return s.length();
        }

        /**
         * Gets the number of spaces at the end of a string.
         * @param s The string to count the trailing spaces of.
         * @return The number of spaces at the end of the given string.
         */
        static int countSpacesAtEnd(@NotNull String s)
        {
            for(int i = 0, r = s.length() - 1; r >= 0; i++, r--)
                if(s.charAt(r) != ' ')
                    return i;

            return s.length();
        }
    }

    /**
     * A pseudo-list containing tokens in order at specific indices. This retains the text representation of the tokens
     * within for quick access, and the spacings (number of spaces) between each token.
     */
    static class TokenList
    {
        //region variables
        /**
         * The string representation of this token list.
         */
        public final String equationAsString;

        /**
         * The tokens in this token list.
         */
        public final List<Token> tokens;

        /**
         * The spacings between each token in this token list. Each integer stored is the number of spaces immediately
         * before the token in {@link #tokens} at the same index. This list contains one more element than
         * {@link #tokens}, where the final element is the number of trailing spaces.
         */
        public final List<Integer> spacings;
        //endregion

        //region initialisation
        /**
         * Creates a new token list.
         * @param equationAsString The string representation of the token list.
         * @param tokens The tokens to be in the token list, in the order they should be in.
         * @param spacingList The number of spaces before each element of tokens at the same integer, with one
         *                    additional integer at the end representing the number of trailing spaces.
         */
        public TokenList(String equationAsString, List<Token> tokens, List<Integer> spacingList)
        {
            this.equationAsString = equationAsString;
            this.tokens = tokens;
            this.spacings = spacingList;
        }
        //endregion

        //region methods
        //region check state
        /**
         * Gets the number of tokens in this token list.
         * @return The number of tokens in this tokenlist.
         */
        public int size()
        { return tokens.size(); }

        /**
         * Gets whether or not this token list is empty.
         * @return True if this token list has no tokens. Otherwise, false.
         */
        public boolean isEmpty()
        { return tokens.isEmpty(); }

        /**
         * Gets whether or not this token list is enclosed in matching brackets.
         * @return True if this token list is enclosed in matching brackets. Otherwise, false.
         */
        public boolean isInBrackets()
        {
            // Assumes that this TokenList doesn't have any bracket mismatches.

            if(tokens.isEmpty())
                return false;

            if(!tokens.get(0).equals(Token.OPEN_BRACKET))
                return false;

            if(!tokens.get(tokens.size() - 1).equals(Token.CLOSE_BRACKET))
                return false;

            int maxToCheck = tokens.size() - 2;
            int bracketDepth = 1;

            for(int i = 1; i <= maxToCheck; i++)
            {
                Token itoken = tokens.get(i);

                if(itoken.equals(Token.OPEN_BRACKET))
                    bracketDepth++;
                else if(itoken.equals(Token.CLOSE_BRACKET))
                {
                    if(--bracketDepth == 0)
                        return false;
                }
            }

            return true;
        }

        /**
         * Gets whether or not the first token of this token list is the given token.
         * @param t The token this token list may start with.
         * @return True if this token list has at least one token and the first token is equal to the given token.
         *         Otherwise, false.
         */
        public boolean startsWith(Token t)
        {
            if(tokens.isEmpty())
                return false;

            return tokens.get(0).equals(t);
        }

        /**
         * Gets whether or not the last token of this token list is the given token.
         * @param t The token this token list may end with.
         * @return True if this token list has at least one token and the last token is equal to the given token.
         * Otherwise, false.
         */
        public boolean endsWith(Token t)
        {
            if(tokens.isEmpty())
                return false;

            return tokens.get(tokens.size() - 1).equals(t);
        }

        /**
         * Gets whether or not this token list contains the given token.
         * @param t The token this token list may contain.
         * @return True if any element in this token list is equal to the given token. Otherwise, false.
         */
        public boolean contains(Token t)
        { return tokens.contains(t); }

        /**
         * Gets whether or not this token list contains any of the given tokens.
         * @param ts The tokens this token list may contain.
         * @return True if any element in this token list is equal to any element of the given collection of tokens.
         *         Otherwise, false.
         */
        public boolean containsAnyOf(Collection<Token> ts)
        {
            for(int i = 0; i < tokens.size(); i++)
                if(ts.contains(tokens.get(i)))
                    return true;

            return false;
        }
        //endregion

        //region get elements

        /**
         * Gets the token in this token list at the given index.
         * @param index The index of this list to get a token from.
         * @return The token in this list at the given index.
         * @throws IndexOutOfBoundsException if index is less than 0, or greater than or equal to this token list's
         *                                   size.
         */
        public Token get(int index)
        { return tokens.get(index); }

        /**
         * Gets the first token in this token list.
         * @return The token at index 0 in this list, or null if this list doesn't have at least one element.
         */
        public Token first()
        {
            if(tokens.isEmpty())
                return null;

            return tokens.get(0);
        }

        /**
         * Gets the last token in this token list.
         * @return The token at the greatest index in this list, or null if this list doesn't have at least one element.
         */
        public Token last()
        {
            if(tokens.isEmpty())
                return null;

            return tokens.get(tokens.size() - 1);
        }
        //endregion

        //region get mutations
        /**
         * Gets a view of this token list where the internal lists of tokens and spacings are explicitly marked as being
         * unmodifiable. If this token list is updated, that will update the contents of the produced token list as
         * well.
         * @return A copy of this tokenlist object, where the internal lists of tokens and spacings are unmodifiable
         * views of the ones in this tokenlist object.
         */
        public TokenList unmodifiable()
        {
            List<Token> newTokens = Collections.unmodifiableList(tokens);
            List<Integer> newSpacings = Collections.unmodifiableList(spacings);
            return new TokenList(equationAsString, newTokens, newSpacings);
        }

        //region sublists
        /**
         * Gets a view of this tokenlist, dropping the given number of elements from the start.
         * @param howMany How many elements to drop from the start.
         * @return A view of this tokenlist, without the first given number of tokens. If howMany is equal to or greater
         *         than the number of elements in this token list, returns an empty token list.
         * @throws IllegalArgumentException if howMany is less than 0.
         *
         */
        public TokenList withoutFirst(int howMany)
        {
            if(howMany < 0)
                throw new IllegalArgumentException("howMany < 0");

            if(size() < howMany)
                return new TokenList("", Collections.emptyList(), Collections.singletonList(0));

            List<Token> newTokens = tokens.subList(howMany, tokens.size());
            List<Integer> newSpacings = spacings.subList(howMany, spacings.size());
            int charsToDrop = 0;

            for(int i = 0; i < howMany; i++)
                charsToDrop += tokens.get(i).text.length() + spacings.get(i);

            String newString = equationAsString.substring(charsToDrop);
            return new TokenList(newString, newTokens, newSpacings);
        }

        /**
         * Gets a view of this tokenlist, dropping the first element from the start.
         * @return A view of this tokenlist, without the first token. If this tokenlist only has one or fewer tokens,
         *         returns an empty tokenlist.
         */
        public TokenList withoutFirst()
        {
            if(size() == 0)
                return new TokenList("", Collections.emptyList(), Collections.singletonList(0));

            List<Token> newTokens = tokens.subList(1, tokens.size());
            List<Integer> newSpacings = spacings.subList(1, spacings.size());
            int charsToDrop = tokens.get(0).text.length() + spacings.get(0);
            String newString = equationAsString.substring(charsToDrop);
            return new TokenList(newString, newTokens, newSpacings);
        }

        /**
         * Gets a view of this tokenlist, dropping the given number of elements from the end.
         * @param howMany How many elements to drop from the end.
         * @return A view of this tokenlist, without the last given number of tokens. If howMany is equal to or greater
         *         than the number of lements in this token list, returns an empty token list.
         * @throws IllegalArgumentException if howMany is less than 0.
         */
        public TokenList withoutLast(int howMany)
        {
            if(howMany < 0)
                throw new IllegalArgumentException("howMany < 0");

            if(size() < howMany)
                return new TokenList("", Collections.emptyList(), Collections.singletonList(0));

            List<Token> newTokens = tokens.subList(0, tokens.size() - howMany);
            List<Integer> newSpacings = spacings.subList(0, spacings.size() - howMany);
            int charsToDrop = 0;

            for(int i = tokens.size() - 1; i >= tokens.size() - howMany; i--)
                charsToDrop += tokens.get(i).text.length() + spacings.get(i + 1);

            String newString = equationAsString.substring(0, equationAsString.length() - charsToDrop);
            return new TokenList(newString, newTokens, newSpacings);
        }

        /**
         * Gets a view of this tokenlist, dropping the last element from the end.
         * @return A view of this tokenlist, without the last token. If this tokenlist only has one or fewer tokens,
         *         returns an empty tokenlist.
         */
        public TokenList withoutLast()
        {
            if(size() == 0)
                return new TokenList("", Collections.emptyList(), Collections.singletonList(0));

            List<Token> newTokens = tokens.subList(0, tokens.size() - 1);
            List<Integer> newSpacings = spacings.subList(0, spacings.size() - 1);
            int charsToDrop = tokens.get(tokens.size() - 1).text.length() + spacings.get(spacings.size() - 1);
            String newString = equationAsString.substring(0, equationAsString.length() - charsToDrop);
            return new TokenList(newString, newTokens, newSpacings);
        }

        /**
         * Gets a view of this tokenlist, dropping the first and last elements from the start and end.
         * @return A view of this tokenlist, without the first and last tokens. If this tokenlist only has two or fewer
         *         tokens, returns an empty tokenlist.
         */
        public TokenList withoutFirstAndLast()
        {
            if(size() <= 1)
                return new TokenList("", Collections.emptyList(), Collections.singletonList(0));

            List<Token> newTokens = tokens.subList(1, tokens.size() - 1);
            List<Integer> newSpacings = spacings.subList(1, spacings.size() - 1);
            int charsToDropFromStart = tokens.get(0).text.length() + spacings.get(0);
            int charsToDropFromEnd = tokens.get(tokens.size() - 1).text.length() + spacings.get(spacings.size() - 1);
            String newString = equationAsString.substring(charsToDropFromStart,
                                                          equationAsString.length() - charsToDropFromEnd);

            return new TokenList(newString, newTokens, newSpacings);
        }

        /**
         * Gets a view of this tokenlist, only containing the elements from fromInclusive to (but not including)
         * toExclusive.
         * @param fromInclusive The first index of the sublist to view from this tokenlist.
         * @param toExclusive The index after the last index of the sublist to view from this tokenlist.
         * @return A view of this tokenlist, only covering the elements from the given lowerbound to the given
         *         upperbound exclusive. Where fromInclusive and toExclusive are equal, this returns an empty tokenlist.
         * @throws IllegalArgumentException if fromInclusive is less than 0, toExclusive is greater than this
         *                                  tokenlist's size, or fromInclusive is greater than toInclusive.
         */
        public TokenList subList(int fromInclusive, int toExclusive)
        {
            if(fromInclusive < 0)
                throw new IllegalArgumentException("fromInclusive < 0");

            if(toExclusive > size())
                throw new IllegalArgumentException("toExclusive > size");

            if(fromInclusive > toExclusive)
                throw new IllegalArgumentException("fromInclusive > toExclusive");


            List<Token> newTokens = tokens.subList(fromInclusive, toExclusive);
            List<Integer> newSpacings = spacings.subList(fromInclusive, toExclusive + 1);

            int charsToDropFromStart = 0;
            int charsToDropFromEnd = 0;

            for(int i = 0; i < fromInclusive; i++)
                charsToDropFromStart += spacings.get(i) + tokens.get(i).text.length();

            for(int i = tokens.size() - 1; i >= toExclusive; i--)
                charsToDropFromEnd += spacings.get(i + 1) + tokens.get(i).text.length();

            String newString = equationAsString.substring(charsToDropFromStart,
                                                          equationAsString.length() - charsToDropFromEnd);

            return new TokenList(newString, newTokens, newSpacings);
        }
        //endregion

        //region split
        /**
         * Gets a list of views of this tokenlist, which are sublists of this tokenlist split by, but not including, the
         * given token.
         * @param t The token to split by.
         * @return <p>Where the given token does not appear in this tokenlist, returns a list containing just a direct
         *         1:1 view of this tokenlist.</p>
         *
         *         <p>Where the given token appears once in this tokenlist, returns a list containing two elements: A
         *         token list containing containing the first elements of this token list before the instance of the
         *         given token, and a token list containing the last elements of this token list after the instance of
         *         the given token.</p>
         *
         *         <p>Where the given token appears multiple times in this tokenlist, returns a list containing a
         *         tokenlist containing the first tokens of this tokenlist up until the first instance of the given
         *         token, tokenlists containing the tokens between instances of this token, and a tokenlist containing
         *         the last tokens of this tokenlist from the last instance of the given token until the end of this
         *         tokenlist.</p>
         *
         *         <p>The returned list is in the order the tokenlists appear as sublists of this tokenlist.</p>
         *
         *         <p>This may produce empty tokenlists, where the given token occurs consecutively, or where the given
         *         token is the first or last token of this tokenlist.</p>
         *
         *         <p>Instances of the given token are not matched where they're within brackets. Specifically, where
         *         the tokens preceding the instance contains at least one unmatched open bracket.</p>
         */
        public List<TokenList> splitBy(Token t)
        {
            List<TokenList> sublists = new ArrayList<>();
            int lastMatch = -1;
            int bracketDepth = 0;

            for(int i = 0; i < tokens.size(); i++)
            {
                Token itoken = tokens.get(i);

                if(itoken.equals(Token.OPEN_BRACKET))
                    bracketDepth++;
                else if(itoken.equals(Token.CLOSE_BRACKET))
                    bracketDepth--;
                else if(bracketDepth == 0 && itoken.equals(t))
                {
                    sublists.add(subList(lastMatch + 1, i));
                    lastMatch = i;
                }
            }

            sublists.add(subList(lastMatch + 1, size()));
            return sublists;
        }

        /**
         * <p>Gets a list of views of this tokenlist, which are sublists of this tokenlist split by, but not including,
         * the given sequence of tokens in order.</p>
         *
         * <p>If there are multiple instance of the given sequence, this will split by the first one.</p>
         * @param sequence The sequence of tokens to split by.
         * @return <p>Where the given sequence of tokens does not appear in this tokenlist in the specified order, or
         *         only partially appears in this tokenlist outwith brackets, returns null.</p>
         *
         *         <p>Where the given sequence appears in this tokenlist in order and outwith brackets, returns a list
         *         of containing a tokenlist containing the first tokens of this tokenlist until the first token of the
         *         sequence, tokenlists containing the tokens between tokens of the sequence, and a tokenlist containing
         *         the last tokens of this tokenlist until the end.</p>
         *
         *         <p>The returned list is in the order the tokenlists appear as sublists of this tokenlist.</p>
         *
         *         <p>The may produce empty tokenlists, where tokens of the given sequence occur consecuritvely, or
         *         where the first or last token of this tokenlist is the first or last token of the given sequence.</p>
         *
         *         <p>Tokens within the given sequence are not matched where they're within brackets. Specifically,
         *         where the tokens preceding it in this tokenlist contains at least one unamtched open bracket.</p>
         */
        public List<TokenList> splitBySequence(List<Token> sequence)
        {
            List<TokenList> result = new ArrayList<>();
            int sequenceIndex = 0;
            Token sequenceToken = sequence.get(sequenceIndex);
            int previousSplitIndex = -1;
            int bracketDepth = 0;

            for(int i = 0; i < tokens.size(); i++)
            {
                Token itoken = tokens.get(i);

                if(itoken.equals(Token.OPEN_BRACKET))
                    bracketDepth++;
                else if(itoken.equals(Token.CLOSE_BRACKET))
                    bracketDepth--;
                else if(bracketDepth == 0 && itoken.equals(sequenceToken))
                {
                    result.add(subList(previousSplitIndex + 1, i));
                    previousSplitIndex = i;

                    sequenceIndex++;

                    if(sequenceIndex == sequence.size())
                        break;

                    sequenceToken = sequence.get(sequenceIndex);
                }
            }

            if(sequenceIndex < sequence.size()) // Not all tokens in sequence were found.
                return null;

            result.add(subList(previousSplitIndex + 1, size()));
            return result;
        }

        /**
         * <p>Gets a list of views of this tokenlist, which are sublists of this tokenlist split by, but not including,
         * the given sequence of tokens in order.</p>
         *
         * <p>This differs from {@link #splitBySequence(List)} by scanning to the end to the start, rather than the
         * start to the end. This means that if there are multiple instance of the given sequence, this will split by
         * the last one.</p>
         * @param sequence The sequence of tokens to split by.
         * @return <p>Where the given sequence of tokens does not appear in this tokenlist in the specified order, or
         *         only partially appears in this tokenlist outwith brackets, returns null.</p>
         *
         *         <p>Where the given sequence appears in this tokenlist in order and outwith brackets, returns a list
         *         of containing a tokenlist containing the first tokens of this tokenlist until the first token of the
         *         sequence, tokenlists containing the tokens between tokens of the sequence, and a tokenlist containing
         *         the last tokens of this tokenlist until the end.</p>
         *
         *         <p>The returned list is in the order the tokenlists appear as sublists of this tokenlist.</p>
         *
         *         <p>The may produce empty tokenlists, where tokens of the given sequence occur consecuritvely, or
         *         where the first or last token of this tokenlist is the first or last token of the given sequence.</p>
         *
         *         <p>Tokens within the given sequence are not matched where they're within brackets. Specifically,
         *         where the tokens preceding it in this tokenlist contains at least one unamtched open bracket.</p>
         */
        public List<TokenList> splitBySequenceInReverse(List<Token> sequence)
        {
            List<TokenList> result = new ArrayList<>();
            int sequenceIndex = sequence.size() - 1;
            Token sequenceToken = sequence.get(sequenceIndex);
            int previousSplitIndex = tokens.size();
            int bracketDepth = 0;

            for(int i = tokens.size() - 1; i >= 0; i--)
            {
                Token itoken = tokens.get(i);

                if(itoken.equals(Token.CLOSE_BRACKET))
                    bracketDepth++;
                else if(itoken.equals(Token.OPEN_BRACKET))
                    bracketDepth--;
                else if(bracketDepth == 0 && itoken.equals(sequenceToken))
                {
                    result.add(subList(i + 1, previousSplitIndex));
                    previousSplitIndex = i;

                    sequenceIndex--;

                    if(sequenceIndex == -1)
                        break;

                    sequenceToken = sequence.get(sequenceIndex);
                }
            }

            if(sequenceIndex >= 0) // Not all tokens in sequence were found.
                return null;

            result.add(subList(0, previousSplitIndex));
            Collections.reverse(result);
            return result;
        }

        /**
         * Gets a list of views of this tokenlist, which are sublists of this tokenlists between the given indices
         * in order.
         * @param points The indices to get a split view of this tokenlist at.
         * @return <p>A list of views of this tokenlist, where each view is a sublist of this tokenlist between the
         *         indices passed, from the lowest to the highest indices provided in order.</p>
         *
         *         <p>Where the given list of points is empty, returns a list just containing this tokenlist.</p>
         * @throws IndexOutOfBoundsException Where any of the indices passed are less than minus 1 or greater than this
         *                                   tokenlist's size.
         */
        public List<TokenList> splitAtPoints(List<Integer> points)
        {
            if(points.isEmpty())
                return Collections.singletonList(this);

            HashSet<Integer> pointsSet = new HashSet<>(points);
            pointsSet.remove(-1);
            pointsSet.remove(size());
            points = new ArrayList<>(pointsSet);
            points.sort(Comparator.naturalOrder());

            if(points.isEmpty())
                return Collections.singletonList(this);

            if(points.get(0) < 0)
                throw new IndexOutOfBoundsException("Lowest point in points < -1. Points must be between (inclusive) "
                                                    + "-1 and size");

            if(points.get(points.size() - 1) > size())
                throw new IndexOutOfBoundsException("Highest point in points > size. Points must be between "
                                                    + "(inclusive) -1 and size");

            List<TokenList> result = new ArrayList<>();
            int previousPoint = -1;

            for(int point : points)
            {
                result.add(subList(previousPoint + 1, point));
                previousPoint = point;
            }

            result.add(subList(previousPoint + 1, size()));
            return result;
        }
        //endregion
        //endregion
        //endregion
    }

    //region Tokens

    /**
     * A representation of an identifiable meaningful portion of a string. (e.g. a variable name, an operator, etc.)
     */
    static class Token
    {
        /**
         * The token representing an open bracket.
         */
        public static final Token OPEN_BRACKET = new Token("(");

        /**
         * The token representing a close bracket.
         */
        public static final Token CLOSE_BRACKET = new Token(")");

        /**
         * The token representing the separator between arguments in a function call.
         */
        public static final Token ARGUMENT_SEPARATOR = new Token(",");

        /**
         * The actual test this token was created from.
         */
        final String text;

        /**
         * Creates a new token from the given text.
         * @param asText The text this token should represent.
         */
        public Token(@NotNull String asText)
        { this.text = asText; }

        /**
         * Gets the text this token represents, what it was created from.
         * @return The text this token represents.
         */
        @Override
        public String toString()
        { return text; }

        /**
         * Two tokens are considered equal where they represent the same text.
         * @param o The other object, likely a token.
         * @return True if the two tokens were equal, otherwise false.
         */
        @Override
        public boolean equals(Object o)
        {
            if(this == o)
                return true;

            if(o == null || getClass() != o.getClass())
                return false;

            Token other = (Token)o;
            return text.equals(other.text);
        }

        @Override
        public int hashCode()
        { return text.hashCode(); }
    }

    /**
     * A representation of a chunk of text that has yet to be tokenised. These may appear around or inbetween identified
     * tokens.
     */
    static class UntokenisedString extends Token
    {
        /**
         * Creates a new untokenised string token.
         * @param s The text this token represents.
         */
        public UntokenisedString(String s)
        { super(s); }
    }

    /**
     * A representation of a number in text.
     */
    static class NumberToken extends Token
    {
        /**
         * This token's value as a number.
         */
        final double value;

        /**
         * Creates a new number token, given its representation in text and the number it's been interpreted as.
         * @param asText The original text representation of this token.
         * @param asNumber The number this token represents.
         */
        public NumberToken(String asText, double asNumber)
        {
            super(asText);
            this.value = asNumber;
        }

        /**
         * Gets the number this token represents.
         * @return The number this token represents.
         */
        public double getValue()
        { return value; }

        /**
         * Two number tokens are considered equal where they represent the same original text, and the same number.
         * @param o The other object, likely a number token.
         * @return True if the two tokens were equal. Otherwise, false.
         */
        @Override
        public boolean equals(Object o)
        {
            if(this == o)
                return true;

            if(o == null || getClass() != o.getClass())
                return false;

            NumberToken other = (NumberToken)o;
            return text.equals(other.text) && Double.compare(other.value, value) == 0;
        }

        @Override
        public int hashCode()
        { return Objects.hash(super.hashCode(), value); }
    }
    //endregion

    //region operators
    /**
     * A definition of an operator for an equation.
     */
    static abstract class Operator
    {
        /**
         * The tokens affixed to operands to invoke this operator. Where an operator has more than 1 token, these are in
         * order as they appear when invoking the operator.
         */
        final List<Token> tokens;

        /**
         * The priority, or "stickiness", of this operator. Where multiple operators are used in conjunction, this
         * determines the order in which the operators are evaluated, where higher priority operator calls are nested in
         * lower priority operator calls. E.g. in "a + b * c", the '*' operator has a higher priority than '+' operator,
         * so the equation could be rephrased as "a + (b * c)".
         */
        final double priority;

        /**
         * The implementation of this operator.
         */
        final OperatorAction action;

        /**
         * Creates a new operator object given a list of affix tokens, a priority, and an implementation.
         * @param tokens The tokens affixed to operands to invoke this operator.
         * @param priority How "sticky" the operator is.
         * @param action The implementation of this operator.
         */
        public Operator(List<Token> tokens, double priority, OperatorAction action)
        {
            this.tokens = Collections.unmodifiableList(tokens);
            this.priority = priority;
            this.action = action;
        }

        /**
         * Gets the priority, or "stickiness", of this operator.
         * @return This operator's priority.
         */
        public double getPriority()
        { return priority; }

        /**
         * Gets the tokens used to invoke this operator, in the order that they must be used.
         * @return A list of the tokens used to invoke this operator. For instance, this would return ["?", ":"] (where
         *         the quoted characters represent tokens) for the conditional ternary operator.
         */
        public List<Token> getTokens()
        { return tokens; }

        /**
         * Attempts to produce an operation object for this operator given a tokenList and context.
         * @param tokenList The token list to be parsed.
         * @param builder The context - the builder to refer to in parsing operands
         * @return An operation using this operator, or null if the given token list was not identifiable as an instance
         *         of this operator being called.
         */
        public abstract Operation tryParse(TokenList tokenList, Builder builder);
    }

    /**
     * A definition of a unary operator (taking one operand) for an equation.
     */
    static abstract class UnaryOperator extends Operator
    {
        /**
         * Creates a new unary operator object given a token, priority, and implementation.
         * @param token The token to be affixed to the operand to invoke the operator.
         * @param priority How "sticky" the operator is.
         * @param action The implementation of this operator.
         */
        public UnaryOperator(Token token, double priority, UnaryOperatorAction action)
        { super(Arrays.asList(token), priority, operands -> action.performOperation(operands[0])); }

        /**
         * Gets the token affixed to an operand to invoke this operator.
         * @return The token to be affixed to an operand to invoke this operator.
         */
        public Token getToken()
        { return this.tokens.get(0); }
    }

    /**
     * A definition of a prefix unary operator (affixed to before an operand) for an equation.
     */
    static class PrefixOperator extends UnaryOperator
    {
        /**
         * Creates a new prefix unary operator object given a token, priority, and implementation.
         * @param token The token to prefix an operand to invoke the operator.
         * @param priority How "sticky" the operator is.
         * @param action The implementation of this operator.
         */
        public PrefixOperator(Token token, double priority, UnaryOperatorAction action)
        { super(token, priority, action); }

        @Override
        public Operation tryParse(TokenList tokenList, Builder builder)
        {
            if(tokenList.size() < 2 || !tokenList.first().equals(getToken()))
                return null;

            return new Operation(builder.tryParse(tokenList.withoutFirst()), this.action);
        }
    }

    /**
     * A definition of a postfix unary operator (affixed to after an operand) for an equation.
     */
    static class PostfixOperator extends UnaryOperator
    {
        /**
         * Creates a new postfix unary operator object given a token, priority, and implementation.
         * @param token The token to postfix an operand to invoke the operator.
         * @param priority How "sticky" the operator is.
         * @param action The implementation of this operator.
         */
        public PostfixOperator(Token token, double priority, UnaryOperatorAction action)
        { super(token, priority, action); }

        @Override
        public Operation tryParse(TokenList tokenList, Builder builder)
        {
            if(tokenList.size() < 2 || !tokenList.last().equals(getToken()))
                return null;

            return new Operation(builder.tryParse(tokenList.withoutLast()), this.action);
        }
    }

    /**
     * A definition of an infix operator (affixed to between operands) for an equation.
     */
    static class InfixOperator extends Operator
    {
        /**
         * Whether the operator is left associative. If false, the operator is right associative.
         */
        final boolean isLeftAssociative;

        /**
         * Creates a new infix operator object given a list of tokens, whether or not the operator is left associative,
         * the priority, and the implementation.
         * @param tokens The list of tokens in order to be placed between operands to invoke this equation.
         * @param isLeftAssociative Whether or not the operator is left associative. If false, the operator is right
         *                          associative. Associativity determines whether operators of the same priority to the
         *                          left or right are "stickier".
         * @param priority How "sticky" the operator is.
         * @param action The implementation of this operator.
         */
        public InfixOperator(List<Token> tokens, boolean isLeftAssociative, double priority, OperatorAction action)
        {
            super(tokens, priority, action);
            this.isLeftAssociative = isLeftAssociative;
        }

        /**
         * Gets whether or not this operator is left associative.
         * @return True if this operator is left associative, false if this operator is right associative.
         */
        public boolean isLeftAssociative()
        { return isLeftAssociative; }

        /**
         * Gets whether or not this operator is right associative.
         * @return True if this operator is right associative, false if this operator is left associative.
         */
        public boolean isRightAssociative()
        { return !isLeftAssociative; }

        /**
         * Gets the number of operands for this infix operator. This should be one more than the number of tokens used
         * to infix this operator.
         * @return The number of operands for this infix operator.
         */
        public int getOperandCount()
        { return this.tokens.size() + 1; }

        @Override
        public Operation tryParse(TokenList tokenList, Builder builder)
        {
            List<TokenList> split = isLeftAssociative ? tokenList.splitBySequenceInReverse(this.tokens)
                                                      : tokenList.splitBySequence(this.tokens);

            return split == null ? null : compileFromOperandTokenLists(split, builder);
        }

        /**
         * Creates a new infix operation given a token list, list of indices in the token list that represents this
         * operator's infix tokens, and the builder used to parse the operands.
         * @param tokenList The list of tokens to parse.
         * @param splitPoints The integer indices of this operator's infix tokens in the given token list.
         * @param builder The builder to use to parse operands.
         * @return A new infix operation of this operator.
         */
        public Operation tryParseFromSplits(TokenList tokenList, List<Integer> splitPoints, Builder builder)
        { return compileFromOperandTokenLists(tokenList.splitAtPoints(splitPoints), builder); }

        /**
         * Creates a new infix operation given a list of token lists representing operators, and the builder used to
         * parse the operands.
         * @param tokenLists The operands.
         * @param builder The builder to use to parse the operands.
         * @return A new infix operation of this operator.
         */
        Operation compileFromOperandTokenLists(List<TokenList> tokenLists, Builder builder)
        {
            List<EquationComponent> components = new ArrayList<>(tokenLists.size());

            for(TokenList tl : tokenLists)
                components.add(builder.tryParse(tl));

            return new Operation(components, action);
        }
    }

    /**
     * A definition of a binary infix operator (affixed to between two operands) for an equation.
     */
    static class BinaryOperator extends InfixOperator
    {
        /**
         * Creates a new infix binary operator from a given token, whether or not it's left associative, the priority,
         * and an implementation.
         * @param token The infix token to be placed between two operands to invoke this operator.
         * @param isLeftAssociative Whether or not this operator is left associative.
         * @param priority How "sticky" this operator is.
         * @param action The implementation.
         */
        public BinaryOperator(Token token, boolean isLeftAssociative, double priority, BinaryOperatorAction action)
        {
            super(Arrays.asList(token),
                  isLeftAssociative,
                  priority,
                  operands -> action.performOperation(operands[0], operands[1]));
        }

        /**
         * Gets the token to be placed between two operands to invoke this operator.
         * @return The token to be placed between two operands to invoke this operator.
         */
        public Token getToken()
        { return this.tokens.get(0); }
    }

    /**
     * A definition of a ternary infix operator (affixed to between three operands) for an equation.
     */
    static class TernaryOperator extends InfixOperator
    {
        /**
         * Creates a new infix ternary operator from given tokens, whether or not it's left associative, the priority,
         * and an implementation.
         * @param a The infix token to be placed between the first and second operands to invoke this operator.
         * @param b The infix token to be placed between the second and third operands to invoke this operand.
         * @param isLeftAssociative Whether or not this operand is left associative.
         * @param priority How "sticky" the operator is.
         * @param action The implementation.
         */
        public TernaryOperator(Token a,
                               Token b,
                               boolean isLeftAssociative,
                               double priority,
                               TernaryOperatorAction action)
        {
            super(Arrays.asList(a, b),
                  isLeftAssociative,
                  priority,
                  operands -> action.performOperation(operands[0], operands[1], operands[2]));
        }

        /**
         * Gets the operator token to be placed between the first and second operands to invoke this operator.
         * @return The operator token to be placed between the first and second operands to invoke this operator.
         */
        public Token getLeftToken()
        { return this.tokens.get(0); }

        /**
         * Gets the operator token to be placed between the second and third operands to invoke this operator.
         * @return The operator token to be placed between the second and third operands to invoke this operator.
         */
        public Token getRightToken()
        { return this.tokens.get(1); }
    }
    //endregion

    //region equation components
    /**
     * An evaluatable component of an equation, corresponding to a single step in the equation evaluation process.
     */
    static abstract class EquationComponent
    {
        /**
         * Evaluates this equation component.
         * @param equationBeingEvaluated The equation this equation component is being evaluated in the context of.
         * @return The result of evaluating this equation component, as a double.
         */
        public abstract double evaluate(Equation equationBeingEvaluated);
    }

    /**
     * An equation component representing an operation - an operator's implementation and the operands to be passed into
     * it. The operands are other equation components to be evaluated to determine the actual values passed into this
     * operation.
     */
    static class Operation extends EquationComponent
    {
        /**
         * The unevaluated operands of this operation.
         */
        List<EquationComponent> components;

        /**
         * The implementation of the operator that will be performed on this operation's operands.
         */
        OperatorAction action;

        /**
         * Creates a new unary operation from an equation component and an operator implementation.
         * @param component The operand.
         * @param action The operator implementation.
         */
        public Operation(EquationComponent component, OperatorAction action)
        { this(Arrays.asList(component), action); }

        /**
         * Creates a new binary operation from a pair of equation components and an operator implementation.
         * @param component1 The left operand.
         * @param component2 The right operand.
         * @param action The operator implementation
         */
        public Operation(EquationComponent component1, EquationComponent component2, OperatorAction action)
        { this(Arrays.asList(component1, component2), action); }

        /**
         * Creates a new operation from a series of equation components and an operator implementation.
         * @param components The operands.
         * @param action The operator implementation.
         */
        public Operation(EquationComponent[] components, OperatorAction action)
        { this(Arrays.asList(components), action); }

        /**
         * Creates a new operation from a series of equation components and an operator implementation.
         * @param components The operands.
         * @param action The operator implementation.
         */
        public Operation(List<EquationComponent> components, OperatorAction action)
        {
            this.components = components;
            this.action = action;
        }

        /**
         * Evaluates the operation as a double. Evaluates each operand in this operation and passes the result into this
         * operation's operator implementation in order, in order to produce a result.
         * @param equationBeingEvaluated The equation this equation component is being evaluated in the context of.
         * @return The result of evaluating this operation, as a double.
         */
        @Override
        public double evaluate(Equation equationBeingEvaluated)
        {
            double[] operands = new double[components.size()];

            for(int i = 0; i < components.size(); i++)
                operands[i] = components.get(i).evaluate(equationBeingEvaluated);

            return action.performOperation(operands);
        }
    }

    /**
     * An equation component that runs a function with a series of arguments - a function name to look up and the
     * function arguments to be passed into it. The arguments are other equation components to be evaluated to determine
     * the actual values passed into this function call.
     */
    static class FunctionCall extends EquationComponent
    {
        /**
         * Ths name of the function being called. This is used to look up the function implementation to run.
         */
        String functionName;

        /**
         * The unevaluated arguments of this function call.
         */
        EquationComponent[] arguments;

        /**
         * Creates a new function call from a function name and a series of arguments.
         * @param functionName The name of the function this calls.
         * @param arguments The function arguments.
         */
        public FunctionCall(String functionName,
                            EquationComponent... arguments)
        {
            this.functionName = functionName;
            this.arguments = arguments;
        }

        /**
         * Runs the function this corresponds to and returns the result as a double. Evaluates each argument in this
         * function call's argument list and passes the results into the function implementation as arguments.
         * @param equationBeingEvaluated The equation this equation component is being evaluated in the context of.
         * @return The result of this function call, as a double.
         */
        @Override
        public double evaluate(Equation equationBeingEvaluated)
        {
            ToDoubleFunction<double[]> f = equationBeingEvaluated.functions.get(functionName);

            if(f == null)
                throw new EquationEvaluation.MissingFunctionException(functionName);

            double[] results = new double[arguments.length];

            for(int i = 0; i < results.length; i++)
                results[i] = arguments[i].evaluate(equationBeingEvaluated);

            return f.applyAsDouble(results);
        }
    }

    /**
     * An equation component referencing a variable.
     */
    static class VariableReference extends EquationComponent
    {
        /**
         * The name of the variable being referenced. This is used to look up the actual value.
         */
        String name;

        /**
         * Creates a new variable reference from a given name.
         * @param name The name of the variable this references.
         */
        public VariableReference(String name)
        { this.name = name; }

        /**
         * Gets the value of the variable this corresponds to and returns it as a double.
         * @param equationBeingEvaluated The equation this equation component is being evaluated in the context of.
         * @return The current value of the variable this corresponds to, as a double.
         */
        @Override
        public double evaluate(Equation equationBeingEvaluated)
        {
            Double result = equationBeingEvaluated.variableValues.get(name);

            if(result == null)
                throw new EquationEvaluation.MissingVariableException(name);

            return result;
        }
    }

    /**
     * An equation components referencing a fixed number.
     */
    static class LiteralNumber extends EquationComponent
    {
        /**
         * The number this references.
         */
        double value;

        /**
         * Creates a new literal number referencing a given number.
         * @param value The number to reference.
         */
        public LiteralNumber(double value)
        { this.value = value; }

        /**
         * Gets the number this references.
         * @param equationBeingEvaluated The equation this equation component is being evaluated in the context of.
         * @return The number this references.
         */
        @Override
        public double evaluate(Equation equationBeingEvaluated)
        { return value; }
    }
    //endregion

    //region actions
    /**
     * An implementation of an operator, given any number of operands.
     */
    @FunctionalInterface
    public interface OperatorAction
    {
        /**
         * Performs this operator implementation on the given operands.
         * @param o The operands passed into the operation.
         * @return The result of this operator implementation on the given operands, as a double.
         */
        double performOperation(double... o);
    }

    /**
     * An implementation of an operator, given one operand.
     */
    @FunctionalInterface
    public interface UnaryOperatorAction
    {
        /**
         * Performs this operator implementation on the given operand.
         * @param o The operand passed into the operation.
         * @return The result of this operator implementation on the given operand, as a double.
         */
        double performOperation(double o);
    }

    /**
     * An implementation of an operator, given two operands.
     */
    @FunctionalInterface
    public interface BinaryOperatorAction
    {
        /**
         * Performs this operator implementation on the given operands.
         * @param l The left operand passed into the operation.
         * @param r The right operand passed into the operation.
         * @return The result of this operator implementation on the given operands, as a double.
         */
        double performOperation(double l, double r);
    }

    /**
     * An implementation of an operator, given three operands.
     */
    @FunctionalInterface
    public interface TernaryOperatorAction
    {
        /**
         * Performs this operator implementation on the given operands.
         * @param l The left operand passed into the operation.
         * @param m The middle operand passed into the operation.
         * @param r The right operand passed into the operation.
         * @return The result of this operator implementation on the given operands, as a double.
         */
        double performOperation(double l, double m, double r);
    }
    //endregion
    //endregion

    //region constants
    /**
     * The default equation builder for constructing equations using the new Equation(String) constructor.
     */
    private static final Builder defaultBuilder = new Builder(true);
    //endregion

    //region variables
    /**
     * <p>The equation component representing the first/root evaluatable component of the equation. This component holds
     * references to other components in the equation, in a tree topology.</p>
     */
    final EquationComponent topLevelComponent;

    /**
     * The variable values provided to this equation by its builder. This may be updated by its builder if the builder
     * is requested to push a new variable value.
     */
    final Map<String, Double> initialVariableValues;

    /**
     * The variable values explicitly redefined on this equation. These override variable values provided to the
     * equation by its builder.
     */
    final Map<String, Double> overwrittenVariableValues;

    /**
     * <p>The variables available to this equation, and their values.</p>
     */
    final Map<String, Double> variableValues;

    /**
     * The functions and their implementations provided to this equation by its builder. This may be updated by its
     * builder if the builder is requested to push a new function implementation.
     */
    final Map<String, ToDoubleFunction<double[]>> initialFunctions;

    /**
     * The functions and their implementations explicitly redefined by this equation. These override function
     * implementations provided to the equation by its builder.
     */
    final Map<String, ToDoubleFunction<double[]>> overwrittenFunctions;

    /**
     * <p>The functions available to this equation, and their implementations. This is independent from the functions
     * map of the builder used to create this equation object, allowing functions to be redefined.</p>
     */
    final Map<String, ToDoubleFunction<double[]>> functions;
    //endregion

    //region initialisation
    /**
     * <p>Creates a new equation object by parsing the given string as an equation, using the default operators,
     * variables, and functions.</p>
     * @param equationAsString The string to be compiled into an equation.
     */
    public Equation(String equationAsString)
    {
        Equation parsedEquation         = defaultBuilder.build(equationAsString);
        this.topLevelComponent          = parsedEquation.topLevelComponent;
        this.initialVariableValues      = parsedEquation.initialVariableValues;
        this.overwrittenVariableValues  = parsedEquation.overwrittenVariableValues;
        this.variableValues             = parsedEquation.variableValues;
        this.initialFunctions           = parsedEquation.functions;
        this.overwrittenFunctions       = parsedEquation.overwrittenFunctions;
        this.functions                  = parsedEquation.functions;
    }

    /**
     * Creates a new equation object with the given top level component, variable map, and function map.
     * @param topLevelComponent The top level component.
     * @param variableValues The variable map. This should be a copy of the one used by the equation builder at the time
     *                       of building.
     * @param functions The function map. This should be a copy of the one used by the equation builder at the time of
     *                  building.
     */
    Equation(EquationComponent topLevelComponent,
             Map<String, Double> variableValues,
             Map<String, ToDoubleFunction<double[]>> functions)
    {
        this.topLevelComponent          = topLevelComponent;
        this.initialVariableValues      = variableValues;
        this.overwrittenVariableValues  = new HashMap<>();
        this.variableValues             = new FallbackMap<>(overwrittenVariableValues, initialVariableValues);
        this.initialFunctions           = functions;
        this.overwrittenFunctions       = new HashMap<>();
        this.functions                  = new FallbackMap<>(overwrittenFunctions, initialFunctions);
    }
    //endregion

    //region methods

    /**
     * Evaluates the equation.
     * @return The result of the equation as a double.
     */
    public double evaluate()
    { return topLevelComponent.evaluate(this); }

    /**
     * <p>Reässigns the value of a variable in this equation. If the equation does not have a variable available to it
     * by the given variable name, does nothing.</p>
     * @param variableName The name of the variable to reässign.
     * @param newValue The value to assign to the variable.
     * @return True if the variable was reässigned successfully. False if it was not, as a result of the variable not
     *         being available to this equation.
     */
    public boolean setVariable(String variableName, double newValue)
    {
        if(!variableValues.containsKey(variableName))
            return false;

        overwrittenVariableValues.put(variableName, newValue);
        return true;
    }

    //region redefine functions
    /**
     * <p>Provides a new implementation of function in this equation. If the equation does not have a function
     * available to it by the given function name, does nothing.</p>
     * @param name The name of the function to redefine.
     * @param f The new implementation of the function.
     * @return True if the function was redefined successfully. False if it was not, as a result of the function not
     *         being available to this equation.
     */
    public boolean redefineFunction(String name, ToDoubleFunction<double[]> f)
    {
        if(!functions.containsKey(name))
            return false;

        overwrittenFunctions.put(name, f);
        return true;
    }

    /**
     * <p>Provides a new implementation of function in this equation. If the equation does not have a function
     * available to it by the given function name, does nothing.</p>
     * @param name The name of the function to redefine.
     * @param f The new implementation of the function, taking no arguments.
     * @return True if the function was redefined successfully. False if it was not, as a result of the function not
     *         being available to this equation.
     */
    public boolean redefineFunction(String name, DoubleSupplier f)
    {
        if(!functions.containsKey(name))
            return false;

        overwrittenFunctions.put(name, x -> f.getAsDouble());
        return true;
    }

    /**
     * <p>Provides a new implementation of function in this equation. If the equation does not have a function
     * available to it by the given function name, does nothing.</p>
     * @param name The name of the function to redefine.
     * @param requiredArgCount The number of arguments required to be passed into the function.
     * @param f The new implementation of the function, taking at least the given number of arguments.
     * @return True if the function was redefined successfully. False if it was not, as a result of the function not
     *         being available to this equation.
     */
    public boolean redefineFunction(String name, int requiredArgCount, ToDoubleFunction<double[]> f)
    {
        if(!functions.containsKey(name))
            return false;

        overwrittenFunctions.put(name, args ->
        {
            if(args.length < requiredArgCount)
                throw new MissingFunctionArgumentsException(name, requiredArgCount, args.length);

            return f.applyAsDouble(args);
        });

        return true;
    }

    /**
     * <p>Provides a new implementation of function in this equation. If the equation does not have a function
     * available to it by the given function name, does nothing.</p>
     * @param name The name of the function to redefine.
     * @param f The new implementation of the function, taking one argument.
     * @return True if the function was redefined successfully. False if it was not, as a result of the function not
     *         being available to this equation.
     */
    public boolean redefineMonoFunction(String name, ToDoubleFunction<Double> f)
    {
        if(!functions.containsKey(name))
            return false;

        overwrittenFunctions.put(name, args ->
        {
            if(args.length < 1)
                throw new MissingFunctionArgumentsException(name, 1, args.length);

            return f.applyAsDouble(args[0]);
        });

        return true;
    }

    /**
     * <p>Provides a new implementation of function in this equation. If the equation does not have a function
     * available to it by the given function name, does nothing.</p>
     * @param name The name of the function to redefine.
     * @param f The new implementation of the function, taking two arguments.
     * @return True if the function was redefined successfully. False if it was not, as a result of the function not
     *         being available to this equation.
     */
    public boolean redefineBiFunction(String name, ToDoubleBiFunction<Double, Double> f)
    {
        if(!functions.containsKey(name))
            return false;

        overwrittenFunctions.put(name, args ->
        {
            if(args.length < 2)
                throw new MissingFunctionArgumentsException(name, 2, args.length);

            return f.applyAsDouble(args[0], args[1]);
        });

        return true;
    }
    //endregion
    //endregion
}
