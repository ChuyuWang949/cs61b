package gitlet;

import java.util.Arrays;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import static gitlet.GitletConstants.*;
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
                commandRunner(restArgs.length == 0, Repository::globalLog);
                break;
            case "find":
                commandRunner(restArgs.length == 1, Repository::find, restArgs[0]);
                break;
            case "status":
                commandRunner(restArgs.length == 0, Repository::status);
                break;
            case "checkout":
                commandRunner(restArgs.length >= 1 && restArgs.length <= 3, Repository::checkout, restArgs);
                break;
            case "branch":
                commandRunner(restArgs.length == 1, Repository::branch, restArgs[0]);
                break;
            case "rm-branch":
                commandRunner(restArgs.length == 1, Repository::rmBranch, restArgs[0]);
                break;
            case "reset":
                commandRunner(restArgs.length == 1, Repository::reset, restArgs[0]);
                break;
            case "merge":
                commandRunner(restArgs.length == 1, Repository::merge, restArgs[0]);
                break;
            default:
                System.out.println("No command with that name exists.");
        }
    }

    public static <T1, T2> void commandRunner (Boolean argsLength, BiConsumer<T1, T2> command, T1 args1, T2 args2) {
        if (!Repository.isinitialized()) {
            System.out.println(UNINITIALIZED_WARNING);
            return;
        } else if (!argsLength) {
            System.out.println(INCORRECT_OPERANDS_WARNING);
            return;
        }
        command.accept(args1, args2);
    }

    public static <T> void commandRunner (Boolean argsLength, Consumer<T> command, T args) {
        if (!Repository.isinitialized()) {
            System.out.println(UNINITIALIZED_WARNING);
            return;
        } else if (!argsLength) {
            System.out.println(INCORRECT_OPERANDS_WARNING);
            return;
        }
        command.accept(args);
    }

    public static void commandRunner (Boolean argsLength, Runnable command) {
        if (!Repository.isinitialized()) {
            System.out.println(UNINITIALIZED_WARNING);
            return;
        } else if (!argsLength) {
            System.out.println(INCORRECT_OPERANDS_WARNING);
            return;
        }
        command.run();
    }
}
