package gitlet;

import java.util.Arrays;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author TODO
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ...
     */
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Please enter a command.");
            System.exit(0);
        }

        String firstArg = args[0];
        String[] restArgs = Arrays.copyOfRange(args, 1, args.length);
        switch(firstArg) {
            case "init":
                if (restArgs.length == 0) {
                    Repository.init();
                } else {
                    System.out.println("INCORRECT_OPERANDS_WARNING");
                }
                break;
            case "add":
                commandRunner(restArgs.length == 1, Repository::add, restArgs[0]);
                break;
            case "commit":
                commandRunner(restArgs.length == 1, Repository::commit, restArgs[0]);
                break;
            case "rm":
                commandRunner(restArgs.length == 1, Repository::rm, restArgs[0]);
                break;
            case "log":
                commandRunner(restArgs.length == 0, Repository::log);
                break;
            case "global-log":
                // TODO: handle the `global-log` command
                break;
            case "find":
                // TODO: handle the `find` command
                break;
            case "status":
                // TODO: handle the `status` command
                break;
            case "checkout":
                commandRunner(restArgs.length >= 1 && restArgs.length <= 3, Repository::checkout, restArgs);
                break;
            case "branch":
                // TODO: handle the `branch` command
                break;
            case "rm-branch":
                // TODO: handle the `rm-branch` command
                break;
            case "reset":
                // TODO: handle the `reset` command
                break;
            case "merge":
                // TODO: handle the `merge` command
                break;
        }
    }

    public static <T1, T2> void commandRunner (Boolean argsLength, BiConsumer<T1, T2> command, T1 args1, T2 args2) {
        if (!Repository.isinitialized()) {
            System.out.println("UNINTIALIZED_WARNING");
        } else if (!argsLength) {
            System.out.println("INCORRECT_OPERANDS_WARNING");
        }
        command.accept(args1, args2);
    }

    public static <T> void commandRunner (Boolean argsLength, Consumer<T> command, T args) {
        if (!Repository.isinitialized()) {
            System.out.println("UNINTIALIZED_WARNING");
        } else if (!argsLength) {
            System.out.println("INCORRECT_OPERANDS_WARNING");
        }
        command.accept(args);
    }

    public static void commandRunner (Boolean argsLength, Runnable command) {
        if (!Repository.isinitialized()) {
            System.out.println("UNINTIALIZED_WARNING");
        } else if (!argsLength) {
            System.out.println("INCORRECT_OPERANDS_WARNING");
        }
        command.run();
    }
}
