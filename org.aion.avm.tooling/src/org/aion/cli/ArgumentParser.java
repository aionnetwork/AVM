package org.aion.cli;

import org.aion.avm.api.Address;
import org.aion.avm.core.util.Helpers;
import org.aion.kernel.TestingKernel;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;


/**
 * Replaces the JCommander-based parser since we wanted a smaller system which was more adaptable to handle our case of multiple
 * transactions in one block (grouping arguments into commands isn't something JCommander could do).
 * Note that the parser uses static state to make the state transition functions in the enums easier to follow but this could be made safer by passing the
 * current parsing state instance into them.  If this needs more complex embeddability (for concurrent testing, for example), this will need to be done but
 * the current approach requires following less indirection to understand.
 * 
 * This is somewhat over-designed but it seemed like the right pattern as it provides flexibility but a largely declarative relationship between arguments.
 */
public class ArgumentParser {
    static String DEFAULT_STORAGE = "./storage";
    static String DEFAULT_SENDER_STRING = TestingKernel.PREMINED_ADDRESS.toString();
    static long DEFAULT_ENERGY_LIMIT = 100_000_000L;

    /**
     * Arguments which do not exist at the top-level, but explain/augment top-level or other inner-level arguments.
     */
    private static enum InnerArg {
        ADDRESS(matches("-a", "--address"), true, (self, address) -> {ArgumentParser.currentCommand.openAddress = address; return null;}),
        SENDER(matches("-sd", "--sender"), true, (self, address) -> {ArgumentParser.currentCommand.senderAddress = address; return null;}),
        ENERGY_LIMIT(matches("-e", "--energy-limit"), true, (self, energyLimit) -> {ArgumentParser.currentCommand.energyLimit = Long.valueOf(energyLimit); return null;}),
        METHOD(matches("-m", "--method"), true, (self, method) -> {ArgumentParser.currentCommand.method = method; return null;}),
        VALUE(matches("--value"), true, (self, balance) -> {ArgumentParser.currentCommand.balance = parseBigInteger(balance); return null;}),
        ARG_INT(matches("-I"), true, (self, arg) -> {ArgumentParser.currentCommand.args.add(Integer.valueOf(arg)); return null;}),
        ARG_LONG(matches("-J"), true, (self, arg) -> {ArgumentParser.currentCommand.args.add(Long.valueOf(arg)); return null;}),
        ARG_SHORT(matches("-S"), true, (self, arg) -> {ArgumentParser.currentCommand.args.add(Short.valueOf(arg)); return null;}),
        ARG_CHAR(matches("-C"), true, (self, arg) -> {ArgumentParser.currentCommand.args.add(Character.valueOf(arg.charAt(0))); return null;}),
        ARG_FLOAT(matches("-F"), true, (self, arg) -> {ArgumentParser.currentCommand.args.add(Float.valueOf(arg)); return null;}),
        ARG_DOUBLE(matches("-D"), true, (self, arg) -> {ArgumentParser.currentCommand.args.add(Double.valueOf(arg)); return null;}),
        ARG_BYTE(matches("-B"), true, (self, arg) -> {ArgumentParser.currentCommand.args.add(Byte.valueOf(arg)); return null;}),
        ARG_BOOLEAN(matches("-Z"), true, (self, arg) -> {ArgumentParser.currentCommand.args.add(Boolean.valueOf(arg)); return null;}),
        ARG_ADDRESS(matches("-A"), true, (self, arg) -> {ArgumentParser.currentCommand.args.add(parseAsAddress(arg)); return null;}),
        ARG_STRING(matches("-T"), true, (self, arg) -> {ArgumentParser.currentCommand.args.add(arg); return null;}),
        ARGS(matches("-a", "--args"), false, (self, ignored) -> {self.runOnNested(); return null;}
            , ARG_INT, ARG_LONG, ARG_SHORT, ARG_CHAR, ARG_FLOAT, ARG_DOUBLE, ARG_BYTE, ARG_BOOLEAN, ARG_ADDRESS, ARG_STRING),
        ;
        
        private final List<String> matches;
        private final boolean extra;
        private final BiFunction<InnerArg, String, Void> changeState;
        private final InnerArg[] nested;
        private InnerArg(String[] matches, boolean extra, BiFunction<InnerArg, String, Void> changeState, InnerArg... nested) {
            this.matches = Arrays.asList(matches);
            this.extra = extra;
            this.changeState = changeState;
            this.nested = nested;
        }
        public boolean doesMatch(String elt) {
            return matches.contains(elt);
        }
        public void parse(String elt) {
            // First, do the common parse.
            String extraArg = null;
            if (this.extra) {
                extraArg = consumeNextArg();
            }
            this.changeState.apply(this, extraArg);
        }
        private void runOnNested() {
            boolean keepTrying = true;
            while (keepTrying && (ArgumentParser.nextIndexToParse < ArgumentParser.args.length)) {
                keepTrying = false;
                String elt = consumeNextArg();
                for (InnerArg inner : this.nested) {
                    if (inner.doesMatch(elt)) {
                        keepTrying = true;
                        inner.parse(elt);
                    }
                }
                if (!keepTrying) {
                    revertArg();
                }
            }
        }
    }

    /**
     * Top-level arguments:  global options or specific commands.
     */
    private static enum TopArg {
        STORAGE(matches("-st", "--storage"), true, (self, path) -> {ArgumentParser.root.storagePath = path; return null;}),
        OPEN(matches("open"), false, (self, ignored) -> {appendNewCommand(Action.OPEN, false); self.runOnNested(); return null;}, InnerArg.ADDRESS),
        DEPLOY(matches("deploy"), true, (self, jar) -> {appendNewCommand(Action.DEPLOY, true).jarPath = jar; self.runOnNested(); return null;}, InnerArg.SENDER, InnerArg.VALUE),
        CALL(matches("call"), true, (self, contract) -> {appendNewCommand(Action.CALL, true).contractAddress = contract; self.runOnNested(); return null;}, InnerArg.SENDER, InnerArg.ENERGY_LIMIT, InnerArg.METHOD, InnerArg.ARGS, InnerArg.VALUE),
        TRANSFER(matches("transfer"), true, (self, address) -> {appendNewCommand(Action.TRANSFER, true).contractAddress = address; self.runOnNested(); return null;}, InnerArg.SENDER, InnerArg.ENERGY_LIMIT, InnerArg.VALUE),
        EXPLORE(matches("explore"), true, (self, contract) -> {appendNewCommand(Action.EXPLORE, false).contractAddress = contract; self.runOnNested(); return null;}),
        BYTES(matches("bytes"), true, (self, jar) -> {appendNewCommand(Action.BYTES, false).jarPath = jar; self.runOnNested(); return null;}),
        ENCODE_CALL(matches("encode-call"), true, (self, contract) -> {appendNewCommand(Action.ENCODE_CALL, false).contractAddress = contract; self.runOnNested(); return null;}, InnerArg.METHOD, InnerArg.ARGS)
        ;
        
        private final List<String> matches;
        private final boolean extra;
        private final BiFunction<TopArg, String, Void> changeState;
        private final InnerArg[] nested;
        private TopArg(String[] matches, boolean extra, BiFunction<TopArg, String, Void> changeState, InnerArg... nested) {
            this.matches = Arrays.asList(matches);
            this.extra = extra;
            this.changeState = changeState;
            this.nested = nested;
        }
        public boolean doesMatch(String elt) {
            return matches.contains(elt);
        }
        public void parse(String elt) {
            // First, do the common parse.
            String extraArg = null;
            if (this.extra) {
                extraArg = consumeNextArg();
            }
            this.changeState.apply(this, extraArg);
        }
        private void runOnNested() {
            boolean keepTrying = true;
            while (keepTrying && (ArgumentParser.nextIndexToParse < ArgumentParser.args.length)) {
                keepTrying = false;
                String elt = consumeNextArg();
                for (InnerArg inner : this.nested) {
                    if (inner.doesMatch(elt)) {
                        keepTrying = true;
                        inner.parse(elt);
                    }
                }
                if (!keepTrying) {
                    revertArg();
                }
            }
        }
    }

    /**
     * The high-level meaning of a top-level command.
     */
    public static enum Action {
        OPEN,
        DEPLOY,
        CALL,
        TRANSFER,
        EXPLORE,
        BYTES,
        ENCODE_CALL
        ;
    }

    /**
     * The description of a single command:  its action and other information it needs to run.
     */
    public static class Command {
        public Action action;
        public String senderAddress = DEFAULT_SENDER_STRING;
        public long energyLimit = DEFAULT_ENERGY_LIMIT;
        
        public String jarPath;
        public String contractAddress;
        public String openAddress;
        public String method;
        public List<Object> args = new ArrayList<>();
        public BigInteger balance = BigInteger.ZERO; // this is an optional parameter, initialized to 0 since if not provided
    }

    /**
     * The top-level invocation.  This includes global options and also the command being run within the invocation.
     */
    public static class Invocation {
        public String errorString = usageString();
        public String storagePath = DEFAULT_STORAGE;
        public List<Command> commands = new ArrayList<>();
        public Action nonBatchingAction = null;
    }

    // These variables represent the state of the parser, as it runs.  They have no meaning outside of a parsing operation.
    // Note that we would need to change these to instance variables if we wanted to make this something which could be concurrently invoked.
    private static String[] args;
    private static int nextIndexToParse;
    private static Invocation root;
    private static Command currentCommand;

    /**
     * The entry-point to parse the arguments given into a high-level invocation.
     * This is the entry-point exposed to external callers.
     * 
     * @param args The command-line arguments to parse
     * @return The high-level invocations described by the arguments.
     */
    public static Invocation parseArgs(String[] args) {
        ArgumentParser.args = args;
        ArgumentParser.nextIndexToParse = 0;
        ArgumentParser.root = new Invocation();
        while (ArgumentParser.nextIndexToParse < args.length) {
            String elt = consumeNextArg();
            // Note that each argument MUST be handled.
            boolean didHandle = false;
            for (TopArg top : TopArg.values()) {
                if (top.doesMatch(elt)) {
                    if (didHandle) {
                        throw new AssertionError("Double-handling");
                    }
                    top.parse(elt);
                    didHandle = true;
                }
            }
            if (!didHandle) {
                throw new IllegalArgumentException("Unknown argument: " + elt);
            }
        }
        return ArgumentParser.root;
    }

    private static String[] matches(String...matches) {
        return matches;
    }

    private static String usageString() {
        // TODO:  This should probably be generated from the description, above, if we want to keep this parser.
        return "Usage: AvmCLI [options] [command] [command options]";
    }

    private static String consumeNextArg() {
        String arg = ArgumentParser.args[ArgumentParser.nextIndexToParse];
        ArgumentParser.nextIndexToParse += 1;
        return arg;
    }

    private static void revertArg() {
        ArgumentParser.nextIndexToParse -= 1;
    }

    private static Command appendNewCommand(Action action, boolean canBeBatched) {
        // "canBeBatched" exists since some of the things we parse as commands cannot be batched (only CALL and DEPLOY can be batched since only they invoke AVM).
        if (!canBeBatched && !ArgumentParser.root.commands.isEmpty()) {
            throw new IllegalArgumentException(action.name() + " cannot be in a batch of commands");
        }
        if ((null != ArgumentParser.root.nonBatchingAction) && !ArgumentParser.root.commands.isEmpty()) {
            throw new IllegalArgumentException(ArgumentParser.root.nonBatchingAction.name() + " cannot be in a batch of commands");
        }
        if (!canBeBatched) {
            ArgumentParser.root.nonBatchingAction = action;
        }
        ArgumentParser.root.errorString = null;
        ArgumentParser.currentCommand = new Command();
        ArgumentParser.currentCommand.action = action;
        ArgumentParser.root.commands.add(ArgumentParser.currentCommand);
        return ArgumentParser.currentCommand;
    }

    private static Address parseAsAddress(String arg) {
        // We want to parse an Address but we first need to read the hex to bytes.
        if (!arg.matches("(0x)?[A-Fa-f0-9]{64}")) {
            throw new IllegalArgumentException("Invalid address: " + arg);
        }
        Address address = new Address(Helpers.hexStringToBytes(arg));
        return address;
    }

    private static BigInteger parseBigInteger(String balance){
        if (balance == null){
            return BigInteger.ZERO;
        } else {
            return new BigInteger(balance);
        }
    }
}
