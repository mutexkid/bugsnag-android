Then("the breadcrumb named {string} has {string} equal to {string}") do |message, path, expected_value|
  match_breadcrumb_metadata(message, path) { |v| Maze.check.equal(expected_value, v) }
end

Then("the breadcrumb named {string} has {string} equal to {int}") do |message, path, expected_value|
  match_breadcrumb_metadata(message, path) { |v| Maze.check.equal(expected_value, v) }
end

Then("the breadcrumb named {string} has {string} is true") do |message, path|
  match_breadcrumb_metadata(message, path) { |v| assert_true(v) }
end

Then("the breadcrumb named {string} has {string} is false") do |message, path|
  match_breadcrumb_metadata(message, path) { |v| assert_false(v) }
end

def match_breadcrumb_metadata message, path
  value = Maze::Helper.read_key_path(Maze::Server.errors.current[:body], "events.0.breadcrumbs")
  found = false
  value.each do |crumb|
    if crumb["name"] == message
      value = Maze::Helper.read_key_path(crumb, path)
      yield value
      found = true
    end
  end
  fail("No breadcrumb matched: #{value}") unless found
end
