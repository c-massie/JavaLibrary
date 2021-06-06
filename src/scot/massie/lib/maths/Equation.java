package scot.massie.lib.maths;

import com.sun.istack.internal.NotNull;
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
import java.util.function.DoubleSupplier;
import java.util.function.ToDoubleBiFunction;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;

import static scot.massie.lib.utils.ControlFlowUtils.*;

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

    public static final class Builder
    {
        //region inner classes
        //region exceptions
        public static class EquationParseException extends RuntimeException
        {
            final TokenList equationSection;
            final TokenList fullEquation;

            EquationParseException(TokenList fullEquation, TokenList equationSection)
            {
                super("Equation was not parsable as an equation: " + fullEquation.equationAsString
                      + "\nSpecifically, this portion: " + equationSection.equationAsString);

                this.fullEquation = fullEquation;
                this.equationSection = equationSection;
            }

            EquationParseException(TokenList fullEquation, TokenList equationSection, String msg)
            {
                super(msg);
                this.fullEquation = fullEquation;
                this.equationSection = equationSection;
            }

            public String getEquationSection()
            { return equationSection.equationAsString.trim(); }

            public String getFullEquation()
            { return fullEquation.equationAsString.trim(); }

            public EquationParseException withFullEquation(TokenList fullEquation)
            { return new EquationParseException(fullEquation, equationSection); }
        }

        public static class DanglingOperatorException extends EquationParseException
        {
            public DanglingOperatorException(TokenList fullEquation, TokenList equationSection)
            {
                super(fullEquation, equationSection,
                      "Equation contained a dangling operator that could not be a prefix nor postfix operator: "
                        + fullEquation.equationAsString
                        + "\nSpecifically, this portion: " + equationSection.equationAsString);
            }

            public DanglingOperatorException(TokenList fullEquation, TokenList equationSection, String msg)
            { super(fullEquation, equationSection, msg); }

            @Override
            public DanglingOperatorException withFullEquation(TokenList fullEquation)
            { return new DanglingOperatorException(fullEquation, equationSection); }
        }

        public static class LeadingNonPrefixOperatorException extends DanglingOperatorException
        {
            public LeadingNonPrefixOperatorException(TokenList fullEquation, TokenList equationSection)
            {
                super(fullEquation, equationSection,
                      "Equation contained a leading operator that could not be a prefix operator: "
                      + fullEquation.equationAsString
                      + "\nSpecifically, this portion: " + equationSection.equationAsString);
            }

            public LeadingNonPrefixOperatorException(TokenList fullEquation, TokenList equationSection, String msg)
            { super(fullEquation, equationSection, msg); }

            @Override
            public LeadingNonPrefixOperatorException withFullEquation(TokenList fullEquation)
            { return new LeadingNonPrefixOperatorException(fullEquation, equationSection); }
        }

        public static class TrailingNonPostfixOperatorException extends DanglingOperatorException
        {
            public TrailingNonPostfixOperatorException(TokenList fullEquation, TokenList equationSection)
            {
                super(fullEquation, equationSection,
                      "Equation contained a trailing operator that could not be a postfix operator: "
                      + fullEquation.equationAsString
                      + "\nSpecifically, this portion: " + equationSection.equationAsString);
            }

            public TrailingNonPostfixOperatorException(TokenList fullEquation, TokenList equationSection, String msg)
            { super(fullEquation, equationSection, msg); }

            @Override
            public TrailingNonPostfixOperatorException withFullEquation(TokenList fullEquation)
            { return new TrailingNonPostfixOperatorException(fullEquation, equationSection); }
        }

        public static class DanglingArgumentSeparatorException extends EquationParseException
        {
            public DanglingArgumentSeparatorException(TokenList fullEquation, TokenList equationSection)
            {
                super(fullEquation, equationSection,
                      "Equation contained an argument list with a dangling separator: "
                      + fullEquation.equationAsString
                      + "\nSpecifically, this portion: " + equationSection.equationAsString);
            }

            public DanglingArgumentSeparatorException(TokenList fullEquation, TokenList equationSection, String msg)
            { super(fullEquation, equationSection, msg); }

            @Override
            public DanglingArgumentSeparatorException withFullEquation(TokenList fullEquation)
            { return new DanglingArgumentSeparatorException(fullEquation, equationSection); }
        }

        public static class LeadingArgumentSeparatorException extends DanglingArgumentSeparatorException
        {
            public LeadingArgumentSeparatorException(TokenList fullEquation, TokenList equationSection)
            {
                super(fullEquation, equationSection,
                      "Equation contained an argument list with a leading separator: "
                      + fullEquation.equationAsString
                      + "\nSpecifically, this portion: " + equationSection.equationAsString);
            }

            public LeadingArgumentSeparatorException(TokenList fullEquation, TokenList equationSection, String msg)
            { super(fullEquation, equationSection, msg); }

            @Override
            public LeadingArgumentSeparatorException withFullEquation(TokenList fullEquation)
            { return new LeadingArgumentSeparatorException(fullEquation, equationSection); }
        }

        public static class TrailingArgumentSeparatorException extends DanglingArgumentSeparatorException
        {

            public TrailingArgumentSeparatorException(TokenList fullEquation, TokenList equationSection)
            {
                super(fullEquation, equationSection,
                      "Equation contained an argument list with a trailing separator: "
                      + fullEquation.equationAsString
                      + "\nSpecifically, this portion: " + equationSection.equationAsString);
            }

            public TrailingArgumentSeparatorException(TokenList fullEquation, TokenList equationSection, String msg)
            { super(fullEquation, equationSection, msg); }

            @Override
            public TrailingArgumentSeparatorException withFullEquation(TokenList fullEquation)
            { return new TrailingArgumentSeparatorException(fullEquation, equationSection); }
        }

        public static class BracketMismatchException extends EquationParseException
        {
            public BracketMismatchException(TokenList equation)
            { super(equation, equation, "Equation contained a bracket mismatch: " + equation.equationAsString); }

            public BracketMismatchException(TokenList equation, String msg)
            { super(equation, equation, msg); }

            @Override
            public BracketMismatchException withFullEquation(TokenList fullEquation)
            { return new BracketMismatchException(fullEquation); }
        }

        public static class UnexpectedCloseBracketException extends BracketMismatchException
        {
            public UnexpectedCloseBracketException(TokenList equation)
            {
                super(equation, "Equation contained a close bracket that didn't correlate to a matching open bracket: "
                                + equation.equationAsString);
            }

            public UnexpectedCloseBracketException(TokenList equation, String msg)
            { super(equation, msg); }

            @Override
            public UnexpectedCloseBracketException withFullEquation(TokenList fullEquation)
            { return new UnexpectedCloseBracketException(fullEquation); }
        }

        public static class UnmatchedOpenBracketException extends BracketMismatchException
        {
            public UnmatchedOpenBracketException(TokenList equation)
            {
                super(equation, "Equation contained an open bracket that didn't correlate to a matching close bracket: "
                                + equation.equationAsString);
            }

            public UnmatchedOpenBracketException(TokenList equation, String msg)
            { super(equation, msg); }

            @Override
            public UnmatchedOpenBracketException withFullEquation(TokenList fullEquation)
            { return new UnmatchedOpenBracketException(fullEquation); }
        }

        public static class UnrecognisedFunctionException extends EquationParseException
        {
            public UnrecognisedFunctionException(String functionName,
                                                 TokenList fullEquation,
                                                 TokenList equationSection)
            {
                super(fullEquation, equationSection, "Equation contained an unrecognised function: " + functionName);
                this.functionName = functionName;
            }

            public UnrecognisedFunctionException(String functionName,
                                                 TokenList fullEquation,
                                                 TokenList equationSection,
                                                 String msg)
            {
                super(fullEquation, equationSection, msg);
                this.functionName = functionName;
            }

            String functionName;

            public String getFunctionName()
            { return functionName; }

            @Override
            public UnrecognisedFunctionException withFullEquation(TokenList fullEquation)
            { return new UnrecognisedFunctionException(functionName, fullEquation, equationSection); }
        }
        //endregion

        static class OperatorPriorityGroup
        {
            final Map<Token, PrefixOperator> prefixOperators = new HashMap<>();
            final Map<Token, PostfixOperator> postfixOperators = new HashMap<>();
            final Tree<Token, InfixOperator> leftAssociativeInfixOperators = new RecursiveTree<>();
            final Tree<Token, InfixOperator> rightAssociativeInfixOperators = new RecursiveTree<>();
        }

        static class OperatorTokenRun
        {
            int startIndexInSource;
            int endIndexInSource;
            int indexOfPivotInRun;
            List<Token> tokens;
            List<Token> tokensBeforePivot;
            List<Token> tokensAfterPivot;

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
        static final double DEFAULT_PRIORITY = 0;
        static final boolean DEFAULT_ASSOCIATIVITY = true; // true == left, false == right.

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

        Set<Token> operatorTokens = new HashSet<>();

        Set<Token> infixOperatorTokens = new HashSet<>();

        Map<List<Token>, InfixOperator>         infixOperators      = new HashMap<>();
        Map<Token, PrefixOperator>              prefixOperators     = new HashMap<>();
        Map<Token, PostfixOperator>             postfixOperators    = new HashMap<>();
        Map<String, ToDoubleFunction<double[]>> functions           = new HashMap<>();
        Map<String, Double>                     variables           = new HashMap<>();

        Map<Double, OperatorPriorityGroup> operatorGroups        = null;
        List<OperatorPriorityGroup>        operatorGroupsInOrder = null;
        //endregion

        //region initialisation
        public Builder()
        { this(true); }

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
        void addDefaultOperators()
        {
            withOperator        ("?", ":", false, -100, (a, b, c) -> a >= 0.5 ? b : c);
            withOperator        ("-",             100,  (l, r)    -> l - r);
            withOperator        ("+",             100,  (l, r)    -> l + r);
            withOperator        ("/",             200,  (l, r)    -> l / r);
            withOperator        ("÷",             200,  (l, r)    -> l / r);
            withOperator        ("*",             200,  (l, r)    -> l * r);
            withOperator        ("×",             200,  (l, r)    -> l * r);
            withOperator        ("%",             300,  (l, r)    -> l % r);
            withPrefixOperator  ("-",             500,  x         -> -x);
            withPrefixOperator  ("+",             500,  x         -> +x);
            withOperator        ("√",             600,  (l, r)    -> Math.pow(r, 1.0 / l));
            withPrefixOperator  ("√",             700,  x         -> Math.sqrt(x));
            withOperator        ("^",      false, 800,  (l, r)    -> Math.pow(l, r));
            withPostfixOperator ("%",             900,  x         -> x / 100);
        }

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

        void addOperator(PrefixOperator op)
        {
            invalidateOperatorGroups();
            Token popToken = op.getToken();
            prefixOperators.put(popToken, op);
            addOperatorToken(popToken);
        }

        void addOperator(PostfixOperator op)
        {
            invalidateOperatorGroups();
            Token popToken = op.getToken();
            postfixOperators.put(popToken, op);
            addOperatorToken(popToken);
        }

        void addOperator(InfixOperator op)
        {
            invalidateOperatorGroups();
            List<Token> iopTokens = op.getTokens();
            infixOperators.put(iopTokens, op);
            addOperatorTokens(iopTokens);
            infixOperatorTokens.addAll(iopTokens);
        }

        void addOperatorToken(Token token)
        {
            if(possibleTokens.add(token))
            {
                possibleTokensInOrder.add(token);
                operatorTokens.add(token);
            }
        }

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
        public Builder withToken(String token)
        {
            addOperatorToken(new Token(token));
            return this;
        }
        //endregion

        //region add variables
        public Builder withVariable(String name, double value)
        {
            variables.put(name, value);
            return this;
        }
        //endregion

        //region add functions
        public Builder withFunction(String name, ToDoubleFunction<double[]> f)
        {
            functions.put(name, f);
            return this;
        }

        public Builder withFunction(String name, DoubleSupplier f)
        { return withFunction(name, value -> f.getAsDouble()); }

        public Builder withFunction(String name, int requiredArgCount, ToDoubleFunction<double[]> f)
        {
            return withFunction(name, args ->
            {
                if(args.length < requiredArgCount)
                    throw new MissingFunctionArgumentsException(name, requiredArgCount, args.length);

                return f.applyAsDouble(args);
            });
        }

        public Builder withMonoFunction(String name, ToDoubleFunction<Double> f)
        {
            return withFunction(name, args ->
            {
                if(args.length < 1)
                    throw new MissingFunctionArgumentsException(name, 1, args.length);

                return f.applyAsDouble(args[0]);
            });
        }

        public Builder withBiFunction(String name, ToDoubleBiFunction<Double, Double> f)
        {
            return withFunction(name, args ->
            {
                if(args.length < 2)
                    throw new MissingFunctionArgumentsException(name, 2, args.length);

                return f.applyAsDouble(args[0], args[1]);
            });
        }
        //endregion

        //region add operators
        public Builder withPrefixOperator(String token, UnaryOperatorAction action)
        { return withPrefixOperator(token, DEFAULT_PRIORITY, action); }

        public Builder withPrefixOperator(String token, double priority, UnaryOperatorAction action)
        {
            addOperator(new PrefixOperator(new Token(token), priority, action));
            return this;
        }

        public Builder withPostfixOperator(String token, UnaryOperatorAction action)
        { return withPostfixOperator(token, DEFAULT_PRIORITY, action); }

        public Builder withPostfixOperator(String token, double priority, UnaryOperatorAction action)
        {
            addOperator(new PostfixOperator(new Token(token), priority, action));
            return this;
        }

        public Builder withOperator(String token, BinaryOperatorAction action)
        { return withOperator(token, DEFAULT_ASSOCIATIVITY, DEFAULT_PRIORITY, action); }

        public Builder withOperator(String token, double priority, BinaryOperatorAction action)
        { return withOperator(token, DEFAULT_ASSOCIATIVITY, priority, action); }

        public Builder withOperator(String token, boolean isLeftAssociative, BinaryOperatorAction action)
        { return withOperator(token, isLeftAssociative, DEFAULT_PRIORITY, action); }

        public Builder withOperator(String token,
                                    boolean isLeftAssociative,
                                    double priority,
                                    BinaryOperatorAction action)
        {
            addOperator(new BinaryOperator(new Token(token), isLeftAssociative, priority, action));
            return this;
        }

        public Builder withOperator(String token1, String token2, TernaryOperatorAction action)
        { return withOperator(token1, token2, DEFAULT_ASSOCIATIVITY, DEFAULT_PRIORITY, action); }

        public Builder withOperator(String token1, String token2, double priority, TernaryOperatorAction action)
        { return withOperator(token1, token2, DEFAULT_ASSOCIATIVITY, priority, action); }

        public Builder withOperator(String token1, String token2, boolean isLeftAssociative, TernaryOperatorAction action)
        { return withOperator(token1, token2, isLeftAssociative, DEFAULT_PRIORITY, action); }

        public Builder withOperator(String token1,
                                    String token2,
                                    boolean isLeftAssociative,
                                    double priority,
                                    TernaryOperatorAction action)
        {
            addOperator(new TernaryOperator(new Token(token1), new Token(token2), isLeftAssociative, priority, action));
            return this;
        }

        public Builder withOperator(String[] tokens, OperatorAction action)
        { return withOperator(Arrays.asList(tokens), action); }

        public Builder withOperator(String[] tokens, boolean isLeftAssociative, OperatorAction action)
        { return withOperator(Arrays.asList(tokens), isLeftAssociative, action); }

        public Builder withOperator(String[] tokens, double priority, OperatorAction action)
        { return withOperator(Arrays.asList(tokens), priority, action); }

        public Builder withOperator(String[] tokens, boolean isLeftAssociative, double priority, OperatorAction action)
        { return withOperator(Arrays.asList(tokens), isLeftAssociative, priority, action); }

        public Builder withOperator(List<String> tokens, OperatorAction action)
        { return withOperator(tokens, DEFAULT_ASSOCIATIVITY, DEFAULT_PRIORITY, action); }

        public Builder withOperator(List<String> tokens, boolean isLeftAssociative, OperatorAction action)
        { return withOperator(tokens, isLeftAssociative, DEFAULT_PRIORITY, action); }

        public Builder withOperator(List<String> tokens, double priority, OperatorAction action)
        { return withOperator(tokens, DEFAULT_ASSOCIATIVITY, priority, action); }

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

        //region parsing
        public Equation build(String toParse)
        {
            if(toParse == null)
                throw new IllegalArgumentException("Cannot parse a null string as an equation.");

            if(toParse.isEmpty())
                throw new IllegalArgumentException("Cannot parse an empty string as an equation.");

            buildOperatorGroups();
            Tokeniser tokeniser = new Tokeniser(possibleTokensInOrder);
            TokenList tokenisation = tokeniser.tokenise(toParse).unmodifiable();
            verifyTokenisationBrackets(tokenisation);
            EquationComponent topLevelComponent;

            try
            { topLevelComponent = tryParse(tokenisation); }
            catch(EquationParseException e)
            { throw e.withFullEquation(tokenisation); }

            return new Equation(topLevelComponent, new HashMap<>(variables), new HashMap<>(functions));
        }

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

        void invalidateOperatorGroups()
        {
            operatorGroups = null;
            operatorGroupsInOrder = null;
        }

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
        boolean startsWithNonPrefixOperator(TokenList tokenList)
        {
            Token first = tokenList.first();
            return operatorTokens.contains(first) && !prefixOperators.containsKey(first);
        }

        boolean endsWithNonPostfixOperator(TokenList tokenList)
        {
            Token last = tokenList.last();
            return operatorTokens.contains(last) && !postfixOperators.containsKey(last);
        }

        // Tokens in a tokenised equation may only be infix operators where:
        //  - There are tokens before and after the run.
        //  - All tokens in the run of operator tokens it's in before it may be postfix operators
        //  - All tokens in the run of operator tokens it's in after it may be prefix operators
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
        VariableReference tryParseVariable(TokenList tokenList)
        {
            String varName = tokenList.equationAsString.trim();
            Double variableValue = variables.get(varName);
            return variableValue == null ? null : new VariableReference(varName);
        }
        //endregion

        //region function parsing
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
                    arguments[i] = tryParse(argTokenLists.get(i));
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

        Operation tryParseOperation(TokenList tokenList, OperatorPriorityGroup opGroup, int opGroupIndex)
        {
            return nullCoalesce(() -> tryParseInfixOperation_rightAssociative(tokenList, opGroup, opGroupIndex),
                                () -> tryParseInfixOperation_leftAssociative(tokenList, opGroup, opGroupIndex),
                                () -> tryParsePrefixOperation(tokenList, opGroup),
                                () -> tryParsePostfixOperation(tokenList, opGroup),
                                () -> null);
        }

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

        Operation tryParsePrefixOperation(TokenList tokenList, OperatorPriorityGroup opGroup)
        {
            PrefixOperator op = opGroup.prefixOperators.get(tokenList.first());

            if(op == null)
                return null;

            return op.tryParse(tokenList, this);
        }

        Operation tryParsePostfixOperation(TokenList tokenList, OperatorPriorityGroup opGroup)
        {
            PostfixOperator op = opGroup.postfixOperators.get(tokenList.last());

            if(op == null)
                return null;

            return op.tryParse(tokenList, this);
        }
        //endregion

        //region number parsing
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

    static class Tokeniser
    {
        List<Token> tokens;

        public Tokeniser(List<Token> tokens)
        { this.tokens = tokens; }

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

        static int countSpacesAtStart(@NotNull String s)
        {
            for(int i = 0; i < s.length(); i++)
                if(s.charAt(i) != ' ')
                    return i;

            return s.length();
        }

        static int countSpacesAtEnd(@NotNull String s)
        {
            for(int i = 0, r = s.length() - 1; r >= 0; i++, r--)
                if(s.charAt(r) != ' ')
                    return i;

            return s.length();
        }
    }

    static class TokenList
    {
        //region variables
        public final String equationAsString;
        public final List<Token> tokens;
        public final List<Integer> spacings;
        //endregion

        //region initialisation
        public TokenList(String equationAsString, List<Token> tokens, List<Integer> spacingList)
        {
            this.equationAsString = equationAsString;
            this.tokens = tokens;
            this.spacings = spacingList;
        }
        //endregion

        //region methods
        //region check state
        public int size()
        { return tokens.size(); }

        public boolean isEmpty()
        { return tokens.isEmpty(); }

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

        public boolean startsWith(Token t)
        {
            if(tokens.isEmpty())
                return false;

            return tokens.get(0).equals(t);
        }

        public boolean endsWith(Token t)
        {
            if(tokens.isEmpty())
                return false;

            return tokens.get(tokens.size() - 1).equals(t);
        }

        public boolean contains(Token t)
        { return tokens.contains(t); }

        public boolean containsAnyOf(Collection<Token> ts)
        {
            for(int i = 0; i < tokens.size(); i++)
                if(ts.contains(tokens.get(i)))
                    return true;

            return false;
        }
        //endregion

        //region get elements
        public Token get(int index)
        { return tokens.get(index); }

        public Token first()
        {
            if(tokens.isEmpty())
                return null;

            return tokens.get(0);
        }

        public Token last()
        {
            if(tokens.isEmpty())
                return null;

            return tokens.get(tokens.size() - 1);
        }
        //endregion

        //region get mutations
        public TokenList unmodifiable()
        {
            List<Token> newTokens = Collections.unmodifiableList(tokens);
            List<Integer> newSpacings = Collections.unmodifiableList(spacings);
            return new TokenList(equationAsString, newTokens, newSpacings);
        }

        //region sublists
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
    static class Token
    {
        public static final Token OPEN_BRACKET = new Token("(");
        public static final Token CLOSE_BRACKET = new Token(")");
        public static final Token ARGUMENT_SEPARATOR = new Token(",");

        final String text;

        public Token(@NotNull String asText)
        { this.text = asText; }

        @Override
        public String toString()
        { return text; }

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

    static class UntokenisedString extends Token
    {
        public UntokenisedString(String s)
        { super(s); }
    }

    static class NumberToken extends Token
    {
        final double value;

        public NumberToken(String asText, double asNumber)
        {
            super(asText);
            this.value = asNumber;
        }

        public double getValue()
        { return value; }

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
    static abstract class Operator
    {
        final List<Token> tokens;
        final double priority;
        final OperatorAction action;

        public Operator(List<Token> tokens, double priority, OperatorAction action)
        {
            this.tokens = Collections.unmodifiableList(tokens);
            this.priority = priority;
            this.action = action;
        }

        public double getPriority()
        { return priority; }

        public List<Token> getTokens()
        { return tokens; }

        public abstract Operation tryParse(TokenList tokenList, Builder builder);
    }

    static abstract class UnaryOperator extends Operator
    {
        public UnaryOperator(Token token, double priority, UnaryOperatorAction action)
        { super(Arrays.asList(token), priority, operands -> action.performOperation(operands[0])); }

        public Token getToken()
        { return this.tokens.get(0); }
    }

    static class PrefixOperator extends UnaryOperator
    {
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

    static class PostfixOperator extends UnaryOperator
    {
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

    static class InfixOperator extends Operator
    {
        final boolean isLeftAssociative;

        public InfixOperator(List<Token> tokens, boolean isLeftAssociative, double priority, OperatorAction action)
        {
            super(tokens, priority, action);
            this.isLeftAssociative = isLeftAssociative;
        }

        public boolean isLeftAssociative()
        { return isLeftAssociative; }

        public boolean isRightAssociative()
        { return !isLeftAssociative; }

        public int getOperandCount()
        { return this.tokens.size() + 1; }

        @Override
        public Operation tryParse(TokenList tokenList, Builder builder)
        {
            List<TokenList> split = isLeftAssociative ? tokenList.splitBySequenceInReverse(this.tokens)
                                                      : tokenList.splitBySequence(this.tokens);

            return split == null ? null : compileFromOperandTokenLists(split, builder);
        }

        public Operation tryParseFromSplits(TokenList tokenList, List<Integer> splitPoints, Builder builder)
        { return compileFromOperandTokenLists(tokenList.splitAtPoints(splitPoints), builder); }

        Operation compileFromOperandTokenLists(List<TokenList> tokenLists, Builder builder)
        {
            List<EquationComponent> components = new ArrayList<>(tokenLists.size());

            for(TokenList tl : tokenLists)
                components.add(builder.tryParse(tl));

            return new Operation(components, action);
        }
    }

    static class BinaryOperator extends InfixOperator
    {
        public BinaryOperator(Token token, boolean isLeftAssociative, double priority, BinaryOperatorAction action)
        {
            super(Arrays.asList(token),
                  isLeftAssociative,
                  priority,
                  operands -> action.performOperation(operands[0], operands[1]));
        }

        public Token getToken()
        { return this.tokens.get(0); }
    }

    static class TernaryOperator extends InfixOperator
    {
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

        public Token getLeftToken()
        { return this.tokens.get(0); }

        public Token getRightToken()
        { return this.tokens.get(1); }
    }
    //endregion

    //region equation components
    static abstract class EquationComponent
    { public abstract double evaluate(Equation equationBeingEvaluated); }

    static class Operation extends EquationComponent
    {
        List<EquationComponent> components;
        OperatorAction action;

        public Operation(EquationComponent component, OperatorAction action)
        { this(Arrays.asList(component), action); }

        public Operation(EquationComponent component1, EquationComponent component2, OperatorAction action)
        { this(Arrays.asList(component1, component2), action); }

        public Operation(EquationComponent[] components, OperatorAction action)
        { this(Arrays.asList(components), action); }

        public Operation(List<EquationComponent> components, OperatorAction action)
        {
            this.components = components;
            this.action = action;
        }

        @Override
        public double evaluate(Equation equationBeingEvaluated)
        {
            double[] operands = new double[components.size()];

            for(int i = 0; i < components.size(); i++)
                operands[i] = components.get(i).evaluate(equationBeingEvaluated);

            return action.performOperation(operands);
        }
    }

    static class FunctionCall extends EquationComponent
    {
        String functionName;
        EquationComponent[] arguments;

        public FunctionCall(String functionName,
                            EquationComponent... arguments)
        {
            this.functionName = functionName;
            this.arguments = arguments;
        }

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

    static class VariableReference extends EquationComponent
    {
        String name;

        public VariableReference(String name)
        { this.name = name; }

        @Override
        public double evaluate(Equation equationBeingEvaluated)
        {
            Double result = equationBeingEvaluated.variableValues.get(name);

            if(result == null)
                throw new EquationEvaluation.MissingVariableException(name);

            return result;
        }
    }

    static class LiteralNumber extends EquationComponent
    {
        double value;

        public LiteralNumber(double value)
        { this.value = value; }

        @Override
        public double evaluate(Equation equationBeingEvaluated)
        { return value; }
    }
    //endregion

    //region actions
    @FunctionalInterface
    public interface OperatorAction
    { double performOperation(double... operands); }

    @FunctionalInterface
    public interface UnaryOperatorAction
    { double performOperation(double operand); }

    @FunctionalInterface
    public interface BinaryOperatorAction
    { double performOperation(double l, double r); }

    @FunctionalInterface
    public interface TernaryOperatorAction
    { double performOperation(double l, double m, double r); }
    //endregion
    //endregion

    //region constants
    private static final Builder defaultBuilder = new Builder(true);
    //endregion

    //region variables
    final EquationComponent topLevelComponent;
    final Map<String, Double> variableValues;
    final Map<String, ToDoubleFunction<double[]>> functions;
    //endregion

    //region initialisation
    public Equation(String equationAsString)
    {
        Equation parsedEquation = defaultBuilder.build(equationAsString);
        this.topLevelComponent  = parsedEquation.topLevelComponent;
        this.variableValues     = parsedEquation.variableValues;
        this.functions          = parsedEquation.functions;
    }

    Equation(EquationComponent topLevelComponent,
             Map<String, Double> variableValues,
             Map<String, ToDoubleFunction<double[]>> functions)
    {
        this.topLevelComponent = topLevelComponent;
        this.variableValues = variableValues;
        this.functions = functions;
    }
    //endregion

    //region methods
    public double evaluate()
    { return topLevelComponent.evaluate(this); }

    public boolean setVariable(String variableName, double newValue)
    {
        if(!variableValues.containsKey(variableName))
            return false;

        variableValues.put(variableName, newValue);
        return true;
    }

    public boolean redefineFunction(String functionName, ToDoubleFunction<double[]> function)
    {
        if(!functions.containsKey(functionName))
            return false;

        functions.put(functionName, function);
        return true;
    }
    //endregion
}
