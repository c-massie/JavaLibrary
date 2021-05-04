package scot.massie.lib.maths;

import com.sun.istack.internal.NotNull;

import java.util.*;
import java.util.function.ToDoubleFunction;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class EquationEvaluator
{
    //region inner classes
    public static final class Builder
    {
        //region inner classes
        //region exceptions
        public static class EquationParseException extends RuntimeException
        {
            public EquationParseException(List<Token> fullEquation, List<Token> equationSection)
            {
                super("Equation was not parsable as an equation: " + Token.listToString(fullEquation)
                      + "\nSpecifically, this portion: " + Token.listToString(equationSection));

                this.fullEquation = fullEquation;
                this.equationSection = equationSection;
            }

            public EquationParseException(List<Token> fullEquation, List<Token> equationSection, String msg)
            {
                super(msg);
                this.fullEquation = fullEquation;
                this.equationSection = equationSection;
            }

            final List<Token> equationSection;
            final List<Token> fullEquation;

            public List<Token> getEquationSection()
            { return equationSection; }

            public String getEquationSectionAsString()
            { return Token.listToString(equationSection); }

            public List<Token> getFullEquation()
            { return fullEquation; }

            public String getFullEquationAsString()
            { return Token.listToString(equationSection); }

            public EquationParseException withFullEquation(List<Token> fullEquation)
            { return new EquationParseException(fullEquation, equationSection); }
        }

        public static class DanglingOperatorException extends EquationParseException
        {
            public DanglingOperatorException(List<Token> fullEquation, List<Token> equationSection)
            {
                super(fullEquation, equationSection,
                      "Equation contained a dangling operator that could not be a prefix nor postfix operator: "
                        + Token.listToString(fullEquation)
                        + "\nSpecifically, this portion: " + Token.listToString(equationSection));
            }

            public DanglingOperatorException(List<Token> fullEquation, List<Token> equationSection, String msg)
            { super(fullEquation, equationSection, msg); }

            @Override
            public DanglingOperatorException withFullEquation(List<Token> fullEquation)
            { return new DanglingOperatorException(fullEquation, equationSection); }
        }

        public static class LeadingNonPrefixOperatorException extends DanglingOperatorException
        {
            public LeadingNonPrefixOperatorException(List<Token> fullEquation, List<Token> equationSection)
            {
                super(fullEquation, equationSection,
                      "Equation contained a leading operator that could not be a prefix operator: "
                      + Token.listToString(fullEquation)
                      + "\nSpecifically, this portion: " + Token.listToString(equationSection));
            }

            public LeadingNonPrefixOperatorException(List<Token> fullEquation, List<Token> equationSection, String msg)
            { super(fullEquation, equationSection, msg); }

            @Override
            public LeadingNonPrefixOperatorException withFullEquation(List<Token> fullEquation)
            { return new LeadingNonPrefixOperatorException(fullEquation, equationSection); }
        }

        public static class TrailingNonPostfixOperatorException extends DanglingOperatorException
        {
            public TrailingNonPostfixOperatorException(List<Token> fullEquation, List<Token> equationSection)
            {
                super(fullEquation, equationSection,
                      "Equation contained a trailing operator that could not be a postfix operator: "
                      + Token.listToString(fullEquation)
                      + "\nSpecifically, this portion: " + Token.listToString(equationSection));
            }

            public TrailingNonPostfixOperatorException(List<Token> fullEquation, List<Token> equationSection, String msg)
            { super(fullEquation, equationSection, msg); }

            @Override
            public TrailingNonPostfixOperatorException withFullEquation(List<Token> fullEquation)
            { return new TrailingNonPostfixOperatorException(fullEquation, equationSection); }
        }
        //endregion

        private static class OperatorPriorityGroup
        {
            final Map<Token, PrefixOperator> prefixOperators = new HashMap<>();
            final Map<Token, PostfixOperator> postfixOperators = new HashMap<>();
            final HashSet<InfixOperator> infixOperators = new HashSet<>();
        }

        private static class OperatorTokenRun
        {
            public OperatorTokenRun(List<Token> source, int runStartIndex, int runEndIndex, int pivotIndexInSource)
            {
                this.startIndexInSource = runStartIndex;
                this.endIndexInSource = runEndIndex;
                this.indexOfPivotInRun = pivotIndexInSource + runStartIndex;
                this.tokens = source.subList(runStartIndex, runEndIndex + 1);
                this.tokensBeforePivot = source.subList(runStartIndex, pivotIndexInSource);
                this.tokensAfterPivot = source.subList(pivotIndexInSource + 1, runEndIndex);
            }

            int startIndexInSource;
            int endIndexInSource;
            int indexOfPivotInRun;
            List<Token> tokens;
            List<Token> tokensBeforePivot;
            List<Token> tokensAfterPivot;
        }
        //endregion

        double DEFAULT_PRIORITY = 0;
        boolean DEFAULT_ASSOCIATIVITY = true; // true == left, false == right.

        String unparsedEquation;

        /**
         * Fixed string tokens that may appear in an equation. Does not include variable names, function names,
         * numbers, or unparsable portions of equation.
         */
        Set<Token> possibleTokens = new HashSet<>();
        {
            possibleTokens.add(Token.OPEN_BRACKET);
            possibleTokens.add(Token.CLOSE_BRACKET);
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
        }

        Set<Token> operatorTokens = new HashSet<>();

        Set<Token> infixOperatorTokens = new HashSet<>();

        Map<List<Token>, InfixOperator> infixOperators;
        Map<Token, PrefixOperator> prefixOperators;
        Map<Token, PostfixOperator> postfixOperators;
        Map<String, ToDoubleFunction<double[]>> functions;
        Map<String, Double> variables;

        public Builder(@NotNull String equation)
        { this(equation, true); }

        public Builder(@NotNull String equation, boolean includeDefaults)
        {
            this(equation,
                 includeDefaults ? getDefaultInfixOperators()   : new HashMap<>(),
                 includeDefaults ? getDefaultPrefixOperators()  : new HashMap<>(),
                 includeDefaults ? getDefaultPostfixOperators() : new HashMap<>(),
                 includeDefaults ? getDefaultFunctions()        : new HashMap<>(),
                 includeDefaults ? getDefaultVariables()        : new HashMap<>());
        }

        private Builder(String equation,
                        Map<List<Token>, InfixOperator> infixOperators,
                        Map<Token, PrefixOperator> prefixOperators,
                        Map<Token, PostfixOperator> postfixOperators,
                        Map<String, ToDoubleFunction<double[]>> functions,
                        Map<String, Double> variables)
        {
            if(equation == null)
                throw new NullPointerException("Equations cannot be null strings.");

            this.unparsedEquation = equation;
            this.infixOperators = infixOperators;
            this.prefixOperators = prefixOperators;
            this.postfixOperators = postfixOperators;
            this.functions = functions;
            this.variables = variables;
        }

        private static Map<List<Token>, InfixOperator> getDefaultInfixOperators()
        {
            return new HashMap<>();
        }

        private static Map<Token, PrefixOperator> getDefaultPrefixOperators()
        {
            return new HashMap<>();
        }

        private static Map<Token, PostfixOperator> getDefaultPostfixOperators()
        {
            return new HashMap<>();
        }

        private static Map<String, ToDoubleFunction<double[]>> getDefaultFunctions()
        {
            return new HashMap<>();
        }

        private static Map<String, Double> getDefaultVariables()
        {
            return new HashMap<>();
        }

        private void addOperator(Operator op)
        {
            if(op instanceof PrefixOperator)
            {
                PrefixOperator pop = (PrefixOperator)op;
                Token popToken = pop.getToken();
                prefixOperators.put(popToken, pop);
                addOperatorToken(popToken);
            }
            else if(op instanceof PostfixOperator)
            {
                PostfixOperator pop = (PostfixOperator)op;
                Token popToken = pop.getToken();
                postfixOperators.put(popToken, pop);
                addOperatorToken(popToken);
            }
            else if(op instanceof InfixOperator)
            {
                InfixOperator iop = (InfixOperator)op;
                List<Token> iopTokens = iop.getTokens();
                infixOperators.put(iopTokens, iop);
                addOperatorTokens(iopTokens);
                infixOperatorTokens.addAll(iopTokens);
            }
            else
            { throw new UnsupportedOperationException("Unrecognised operator type: " + op.getClass().getName()); }
        }

        private void addOperatorToken(Token token)
        {
            if(possibleTokens.add(token))
            {
                possibleTokensInOrder.add(token);
                operatorTokens.add(token);
            }
        }

        private void addOperatorTokens(List<Token> tokens)
        {
            for(Token token : tokens)
                if(possibleTokens.add(token))
                {
                    possibleTokensInOrder.add(token);
                    operatorTokens.add(token);
                }
        }

        public Builder withToken(String token)
        {
            addOperatorToken(new Token(token));
            return this;
        }

        public Builder withVariable(String name, double value)
        {
            variables.put(name, value);
            return this;
        }

        public Builder withFunction(String name, ToDoubleFunction<double[]> f)
        {
            functions.put(name, f);
            return this;
        }

        public Builder withPrefixOperator(String token, UnaryOperatorAction action)
        {
            addOperator(new PrefixOperator(new Token(token), DEFAULT_PRIORITY, action));
            return this;
        }

        public Builder withPostfixOperator(String token, UnaryOperatorAction action)
        {
            addOperator(new PostfixOperator(new Token(token), DEFAULT_PRIORITY, action));
            return this;
        }

        public Builder withOperator(String token, BinaryOperatorAction action)
        {
            addOperator(new BinaryOperator(new Token(token), DEFAULT_ASSOCIATIVITY, DEFAULT_PRIORITY, action));
            return this;
        }

        public Builder withOperator(String token1, String token2, TernaryOperatorAction action)
        {
            addOperator(new TernaryOperator(new Token(token1), new Token(token2), DEFAULT_ASSOCIATIVITY, DEFAULT_PRIORITY, action));
            return this;
        }

        public Builder withOperator(String[] tokens, OperatorAction action)
        {
            List<Token> ts = new ArrayList<>(tokens.length);

            for(String i : tokens)
                ts.add(new Token(i));

            addOperator(new InfixOperator(ts, DEFAULT_ASSOCIATIVITY, DEFAULT_PRIORITY, action));
            return this;
        }

        public Builder withOperator(List<String> tokens, OperatorAction action)
        {
            List<Token> ts = new ArrayList<>(tokens.size());

            for(String i : tokens)
                ts.add(new Token(i));

            addOperator(new InfixOperator(ts, DEFAULT_ASSOCIATIVITY, DEFAULT_PRIORITY, action));
            return this;
        }

        public EquationEvaluator build()
        {
            Map<Double, OperatorPriorityGroup> opGroups = new HashMap<>();

            for(Map.Entry<Token, PrefixOperator> e : prefixOperators.entrySet())
            {
                opGroups.computeIfAbsent(e.getValue().priority, x -> new OperatorPriorityGroup())
                        .prefixOperators
                        .put(e.getKey(), e.getValue());
            }

            for(Map.Entry<Token, PostfixOperator> e : postfixOperators.entrySet())
            {
                opGroups.computeIfAbsent(e.getValue().priority, x -> new OperatorPriorityGroup())
                        .postfixOperators
                        .put(e.getKey(), e.getValue());
            }

            for(Map.Entry<List<Token>, InfixOperator> e : infixOperators.entrySet())
            {
                opGroups.computeIfAbsent(e.getValue().priority, x -> new OperatorPriorityGroup())
                        .infixOperators
                        .add(e.getValue());
            }

            Tokeniser tokeniser = new Tokeniser(possibleTokensInOrder);
            List<Token> equationTokens
                    = Collections.unmodifiableList(new ArrayList<>(tokeniser.tokenise(unparsedEquation)));

            throw new UnsupportedOperationException("Not implemented yet");
        }

        private EquationComponent tryParse(List<Token> toParse, Map<Double, OperatorPriorityGroup> opGroups)
        {
            throw new UnsupportedOperationException("Not implemented yet");
        }

        // Tokens in a tokenised equation may only be infix operators where:
        //  - There are tokens before and after the run.
        //  - All tokens in the run of operator tokens it's in before it may be postfix operators
        //  - All tokens in the run of operator tokens it's in after it may be prefix operators
        private boolean canBeInfixOperatorToken(List<Token> tokens, int tokenIndex)
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
        private OperatorTokenRun getOpRun(List<Token> tokens, int indexToGetOpRunThatContainsIt)
        {
            int min = -1, max = -1;

            for(int i = indexToGetOpRunThatContainsIt - 1; i > 0; i--)
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
    }

    private static class Tokeniser
    {
        List<Token> tokens;

        public Tokeniser(List<Token> tokens)
        { this.tokens = tokens; }

        public LinkedList<Token> tokenise(String s)
        {
            LinkedList<Token> result = new LinkedList<>();
            result.add(new UntokenisedString(s.trim()));

            for(int tokenIndex = tokens.size() - 1; tokenIndex >= 0; tokenIndex--)
            {
                Token possibleToken = tokens.get(tokenIndex);
                String possibleTokenTextEscaped = Pattern.quote(possibleToken.toString());

                ListIterator<Token> resultIterator = result.listIterator();

                while(resultIterator.hasNext())
                {
                    Token resultToken = resultIterator.next();

                    if(!(resultToken instanceof UntokenisedString))
                        continue;

                    String rtString = resultToken.toString();
                    String[] rtSplit = rtString.split(possibleTokenTextEscaped);

                    if(rtSplit.length > 1)
                    {
                        resultIterator.remove();
                        int rtSplitLastIndex = rtSplit.length - 1;

                        for(int i = 0; i < rtSplitLastIndex; i++)
                        {
                            resultIterator.add(new UntokenisedString(rtSplit[i].trim()));
                            resultIterator.add(possibleToken);
                        }

                        resultIterator.add(new UntokenisedString(rtSplit[rtSplitLastIndex].trim()));
                    }
                }
            }

            tokeniseNumbers(result);
            return result;
        }

        /**
         * Replaces all {@link UntokenisedString untokenised strings} in a list of tokens that are parseable as numbers
         * with {@link NumberToken number tokens}.
         * @param tokens The list of tokens to parse the numbers in.
         */
        private void tokeniseNumbers(LinkedList<Token> tokens)
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
    }

    //region Tokens
    private static class Token
    {
        public static final Token OPEN_BRACKET = new Token("(");
        public static final Token CLOSE_BRACKET = new Token(")");

        final String text;

        public Token(@NotNull String asText)
        { this.text = asText; }

        public static String listToString(List<Token> tokenList)
        {
            if(tokenList == null)
                throw new IllegalArgumentException("tokenList cannot be null.");

            return tokenList.stream()
                            .map(Token::toString)
                            .collect(Collectors.joining(" "));
        }

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

    private static class UntokenisedString extends Token
    {
        public UntokenisedString(String s)
        { super(s); }
    }

    private static class NumberToken extends Token
    {
        public NumberToken(String asText, double asNumber)
        {
            super(asText);
            this.value = asNumber;
        }

        final double value;

        public double getValue()
        { return value; }
    }
    //endregion

    //region operators
    private static abstract class Operator
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

        public abstract EquationComponent tryParse(List<Token> toParse,
                                                   Builder builder,
                                                   Map<Double, Builder.OperatorPriorityGroup> opGroups);
    }

    private static abstract class UnaryOperator extends Operator
    {
        public UnaryOperator(Token token, double priority, UnaryOperatorAction action)
        { super(Arrays.asList(token), priority, operands -> action.performOperation(operands[0])); }

        public Token getToken()
        { return this.tokens.get(0); }
    }

    private static class PrefixOperator extends UnaryOperator
    {
        public PrefixOperator(Token token, double priority, UnaryOperatorAction action)
        { super(token, priority, action); }

        @Override
        public EquationComponent tryParse(List<Token> toParse,
                                          Builder builder,
                                          Map<Double, Builder.OperatorPriorityGroup> opGroups)
        {
            if((toParse.size() < 2) || (!toParse.get(0).equals(getToken())))
                return null;

            return new Operation(builder.tryParse(toParse.subList(1, toParse.size()), opGroups), this.action);
        }
    }

    private static class PostfixOperator extends UnaryOperator
    {
        public PostfixOperator(Token token, double priority, UnaryOperatorAction action)
        { super(token, priority, action); }

        @Override
        public EquationComponent tryParse(List<Token> toParse, Builder builder, Map<Double, Builder.OperatorPriorityGroup> opGroups)
        {
            if((toParse.size() < 2) || (!toParse.get(toParse.size() - 1).equals(getToken())))
                return null;

            return new Operation(builder.tryParse(toParse.subList(0, toParse.size() - 1), opGroups), this.action);
        }
    }

    private static class InfixOperator extends Operator
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
        public EquationComponent tryParse(List<Token> toParse, Builder builder, Map<Double, Builder.OperatorPriorityGroup> opGroups)
        {
            List<List<Token>> split = splitTokenList(toParse, builder);

            if(split == null)
                return null;

            List<EquationComponent> components = new ArrayList<>(split.size());

            for(List<Token> sublist : split)
                components.add(builder.tryParse(toParse, opGroups));

            return new Operation(components, action);
        }

        private List<List<Token>> splitTokenList(List<Token> tokenList, Builder builder)
        {
            if(isLeftAssociative)
            {
                // TO DO: Implement
                throw new UnsupportedOperationException("Not implemented yet.");
            }
            else
            {
                List<List<Token>> result = new ArrayList<>(getOperandCount());
                int tokenLookingForIndex = 0;
                Token tokenLookingFor = this.tokens.get(tokenLookingForIndex);
                int indexOfLastTokenFound = -1;
                int bracketDepth = 0;

                LinkedList<Token> current;

                for(int i = 0; i < tokenList.size(); i++)
                {
                    Token itoken = tokenList.get(i);

                    if(itoken.equals(Token.OPEN_BRACKET))
                        bracketDepth++;
                    else if(itoken.equals(Token.CLOSE_BRACKET))
                    {
                        if(--bracketDepth < 0)
                            return null;
                    }
                    else if(bracketDepth == 0)
                    {
                        if(itoken.equals(tokenLookingFor) && builder.canBeInfixOperatorToken(tokenList, i))
                        {
                            result.add(tokenList.subList(indexOfLastTokenFound + 1, i));
                            indexOfLastTokenFound = i;

                            if(++tokenLookingForIndex == this.tokens.size())
                                break;

                            tokenLookingFor = this.tokens.get(tokenLookingForIndex);
                        }
                    }
                }

                if(tokenLookingForIndex < this.tokens.size())
                    return null;

                result.add(tokenList.subList(indexOfLastTokenFound + 1, tokenList.size()));
                return result;
            }
        }
    }

    private static class BinaryOperator extends InfixOperator
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

    private static class TernaryOperator extends InfixOperator
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
    private static abstract class EquationComponent
    {
        public abstract double evaluate();
    }

    private static class Operation extends EquationComponent
    {
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

        List<EquationComponent> components;
        OperatorAction action;

        @Override
        public double evaluate()
        {
            double[] operands = new double[components.size()];

            for(int i = 0; i < components.size(); i++)
                operands[i] = components.get(i).evaluate();

            return action.performOperation(operands);
        }
    }

    private static class FunctionCall extends EquationComponent
    {
        public FunctionCall(Map<String, ToDoubleFunction<double[]>> functionMap,
                            String functionName,
                            EquationComponent... arguments)
        {
            this.functionMap = functionMap;
            this.functionName = functionName;
            this.arguments = arguments;
        }

        String functionName;
        EquationComponent[] arguments;
        Map<String, ToDoubleFunction<double[]>> functionMap;

        @Override
        public double evaluate()
        {
            ToDoubleFunction<double[]> f = functionMap.get(functionName);

            if(f == null)
                throw new EquationEvaluation.MissingFunctionException(functionName);

            double[] results = new double[arguments.length];

            for(int i = 0; i < results.length; i++)
                results[i] = arguments[i].evaluate();

            return f.applyAsDouble(results);
        }
    }

    private static class VariableReference extends EquationComponent
    {
        public VariableReference(Map<String, Double> variableValues, String name)
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
                throw new EquationEvaluation.MissingVariableException(name);

            return result;
        }
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
    //endregion

    //region actions
    @FunctionalInterface
    public interface OperatorAction
    {
        double performOperation(double... operands);
    }

    @FunctionalInterface
    public interface UnaryOperatorAction
    {
        double performOperation(double operand);
    }

    @FunctionalInterface
    public interface BinaryOperatorAction
    {
        double performOperation(double a, double b);
    }

    @FunctionalInterface
    public interface TernaryOperatorAction
    {
        double performOperation(double a, double b, double c);
    }
    //endregion
    //endregion

    //region variables
    final EquationComponent topLevelComponent;
    //endregion

    //region initialisation
    private EquationEvaluator(EquationComponent topLevelComponent)
    {
        this.topLevelComponent = topLevelComponent;
    }
    //endregion

    //region methods

    //endregion
}
