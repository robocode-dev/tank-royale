using System;
using System.Collections.Generic;
using System.Linq;
using System.Reflection;
using System.Text.RegularExpressions;
using NUnit.Framework;

namespace Robocode.TankRoyale.BotApi.Tests;

[TestFixture]
public class TestPurposeArchitectureTest
{
    private static readonly Regex AcceptanceId = new("^(?:[A-Z][A-Z0-9]*-)+\\d+[a-z]?$", RegexOptions.Compiled);

    [Test]
    [Category("Arch")]
    public void EveryNUnitTestHasExactlyOneEffectivePurpose()
    {
        var failures = new List<string>();
        var assembly = typeof(TestPurposeArchitectureTest).Assembly;

        foreach (var type in assembly.GetTypes())
        {
            var classPurposes = type.GetCustomAttributes<CategoryAttribute>(inherit: true)
                .Select(category => category.Name)
                .Where(IsPurpose)
                .ToList();

            foreach (var method in type.GetMethods(BindingFlags.Instance | BindingFlags.Static | BindingFlags.Public | BindingFlags.NonPublic))
            {
                if (!IsTestMethod(method))
                {
                    continue;
                }

                var methodPurposes = method.GetCustomAttributes<CategoryAttribute>(inherit: false)
                    .Select(category => category.Name)
                    .Where(IsPurpose)
                    .ToList();
                var effective = methodPurposes.Count > 0 ? methodPurposes : classPurposes;

                if (EffectivePurposeCount(effective) != 1)
                {
                    failures.Add($"{type.FullName}.{method.Name}: [{string.Join(", ", effective)}]");
                }
            }
        }

        Assert.That(failures, Is.Empty, string.Join(Environment.NewLine, failures));
    }

    private static bool IsTestMethod(MethodInfo method) => method.GetCustomAttributes()
        .Any(attribute => attribute.GetType().Name is "TestAttribute" or "TestCaseAttribute" or "TestCaseSourceAttribute" or "TheoryAttribute");

    private static bool IsPurpose(string value) => IsAcceptanceId(value) || value is "Unit" or "Sanity" or "Arch";

    private static bool IsAcceptanceId(string value)
        => AcceptanceId.IsMatch(value);

    private static int EffectivePurposeCount(IReadOnlyCollection<string> purposes)
    {
        var acceptanceCount = purposes.Count(IsAcceptanceId);
        return acceptanceCount > 0 ? acceptanceCount : purposes.Count(purpose => purpose is "Unit" or "Sanity" or "Arch");
    }
}
