package ch.nerdin.minecraft.plugin.sync;

import ch.vorburger.minecraft.osgi.api.CommandRegistration;
import com.google.common.collect.ImmutableList;
import java.util.List;
import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.text.Text;

public class HelloWorldCommand implements CommandRegistration, CommandExecutor {

    @Override
    public List<String> aliases() {
        return ImmutableList.of("helloworld", "hello", "test");
    }

    @Override
    public CommandCallable callable() {
        return CommandSpec.builder()
                .description(Text.of("Hello World Command"))
                .executor(this)
                .build();
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        src.sendMessage(Text.of("Hello to the openshift World!"));
        return CommandResult.success();
    }

}

