package coffee.khyonieheart.brimstone.common;

import java.util.List;

import coffee.khyonieheart.brimstone.pylon.PylonManager;
import coffee.khyonieheart.hyacinth.command.parser.SuggestionGenerator;

public class PylonGenerator implements SuggestionGenerator
{
	@Override
	public List<String> generateSuggestions() 
	{
		return PylonManager.getPylons().stream()
			.map((pylon) -> { return pylon.getName(); })
			.toList();
	}	
}
